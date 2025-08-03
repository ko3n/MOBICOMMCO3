package com.mobdeve.s16.group6

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s16.group6.data.*
import com.mobdeve.s16.group6.reminders.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepo = TaskRepo(application)
    private val householdDao = AppDatabase.getInstance(application).householdDao()
    private val appCtx = application

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _householdMembers = MutableStateFlow<List<Person>>(emptyList())
    val householdMembers: StateFlow<List<Person>> = _householdMembers.asStateFlow()

    private var currentHouseholdId: Int? = null
    private var currentPersonId: Int? = null

    private val _taskStatusFilter = MutableStateFlow<TaskStatus?>(null)
    open val taskStatusFilter: StateFlow<TaskStatus?> = _taskStatusFilter

    open val filteredTasks: StateFlow<List<Task>> = tasks
        .combine(_taskStatusFilter) { tasks, statusFilter ->
            statusFilter?.let { filter ->
                tasks.filter { it.status == filter }
            } ?: tasks
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // post-firebase-sync feed logic
    private val firebaseFeedRepo = FirebaseFeedRepo()
    private val _feedEvents = MutableStateFlow<List<FeedEvent>>(emptyList())
    val feedEvents: StateFlow<List<FeedEvent>> = _feedEvents.asStateFlow()

    // pre-firebase-sync feed logic
//    private val _feedEvents = MutableStateFlow<List<FeedEvent>>(emptyList())
//    val feedEvents: StateFlow<List<FeedEvent>> = _feedEvents.asStateFlow()

    fun pushFeedEvent(eventType: EventType, task: Task, userName: String) {
        val now = System.currentTimeMillis()

        val event = FeedEvent(
            eventType = eventType,
            taskTitle = task.title,
            userName = userName,
            timestamp = now
        )

        val existing = _feedEvents.value

        val alreadyExists = existing.any {
            it.eventType == event.eventType &&
                    it.taskTitle == event.taskTitle &&
                    it.userName == event.userName &&
                    (now - it.timestamp) < 5000 // 5 seconds
        }

        if (!alreadyExists) {
            _feedEvents.value = listOf(event) + existing
            Log.d("FeedDebug", "Feed event pushed: ${event.eventType} - ${event.taskTitle} by ${event.userName}")

            //firebase push for feed events
            viewModelScope.launch {
                val household = currentHouseholdId?.let { householdDao.getHouseholdById(it) }
                val firebaseId = household?.firebaseId
                if (!firebaseId.isNullOrBlank()) {
                    firebaseFeedRepo.pushFeedEvent(firebaseId, event)
                } else {
                    Log.w("FeedDebug", "No firebaseId available for household — skipping feed upload")
                }
            }

        } else {
            Log.d("FeedDebug", "Duplicate feed event suppressed: ${event.taskTitle} by ${event.userName}")
        }
    }

    companion object {
        private const val TAG = "TaskViewModel"
    }

    /**
     * Initializes the ViewModel with the context of the current person and household.
     * This method must be called when navigating to the TaskScreen.
     * It fetches tasks for the given person and all household members for assignee selection.
     * It now syncs tasks from Firebase before collecting local data.
     */
    fun initialize(personId: Int, householdName: String, householdEmail: String) {
        Log.d(TAG, "Initializing for personId: $personId, householdName: $householdName, householdEmail: $householdEmail")
        viewModelScope.launch {
            val household = householdDao.findByNameOrEmail(householdName, householdEmail)
            household?.let {
                currentHouseholdId = it.id
                currentPersonId = personId
                Log.d(TAG, "Household found: ID=${it.id}, Name=${it.name}. Starting sync and data collection.")

                // 1. Sync tasks from Firebase to Room before collecting local tasks
                syncTasksFromFirebaseToRoom(it)

                // 2. Collect local tasks and members
                launch {
                    taskRepo.getTasksForPerson(personId, it.id).collectLatest { taskList ->
                        _tasks.value = taskList
                        Log.d(TAG, "Tasks collected: ${taskList.size} tasks for personId $personId, householdId ${it.id}")
                    }
                }
                launch {
                    taskRepo.getAllPeopleForHousehold(it.id).collectLatest { members ->
                        _householdMembers.value = members
                        Log.d(TAG, "Household members collected: ${members.size} members for householdId ${it.id}")
                    }
                }
            } ?: run {
                Log.e(TAG, "Household NOT found for name: $householdName, email: $householdEmail. Cannot load tasks or members.")
                Toast.makeText(appCtx, "Error: Household not found for tasks. Please log in again.", Toast.LENGTH_LONG).show()
                _householdMembers.value = emptyList()
                _tasks.value = emptyList()
            }
        }
    }

    /**
     * Adds a new task to the database.
     * Requires currentHouseholdId to be set from a successful initialization.
     */
    fun addTask(
        title: String,
        description: String?,
        dueDateMillis: Long?,
        priority: TaskPriority,
        assigneeId: Int?,
        isRecurring: Boolean,
        recurringInterval: RecurringInterval?
    ) {
        if (currentHouseholdId == null || currentPersonId == null) {
            val errorMessage = "Task creation failed: Household or Person context not initialized."
            Log.e(TAG, errorMessage)
            Toast.makeText(appCtx, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            val household = householdDao.getHouseholdById(currentHouseholdId!!)
            val firebaseHouseholdId = household?.firebaseId
            if (firebaseHouseholdId == null) {
                Log.e(TAG, "Cannot add task: Household not synced to Firebase!")
                Toast.makeText(appCtx, "Error: Household not synced to cloud.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val baseTask = Task(
                title = title,
                description = description,
                dueDateMillis = dueDateMillis,
                priority = priority,
                assigneeId = assigneeId,
                isRecurring = isRecurring,
                recurringInterval = recurringInterval,
                householdId = currentHouseholdId!!
            )
            val newTask = baseTask.copy(status = calculateStatus(baseTask))
            Log.d(TAG, "Attempting to add task: $newTask")
            try {
                val taskId = taskRepo.addTask(newTask, firebaseHouseholdId).toInt()
                ReminderScheduler.scheduleFullReminderSet(appCtx, taskId, newTask.dueDateMillis ?: return@launch)
                Log.d(TAG, "Task added and reminder scheduled, taskID = $taskId.")

                val personName = _householdMembers.value.find { it.id == currentPersonId }?.name ?: "Someone"
                pushFeedEvent(EventType.CREATED, newTask, personName)

            } catch (e: Exception) {
                Log.e(TAG, "Error adding task: ${e.message}", e)
                Toast.makeText(appCtx, "Failed to add task: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Updates an existing task in the database.
     */
    fun updateTask(task: Task, pushToFeed: Boolean = true) {
        viewModelScope.launch {
            Log.d(TAG, "Attempting to update task: $task")
            try {
                val updatedStatus = calculateStatus(task)
                val updatedTask = task.copy(status = updatedStatus)
                taskRepo.updateTask(updatedTask)
                ReminderScheduler.cancelReminder(appCtx, updatedTask.id)
                ReminderScheduler.scheduleFullReminderSet(appCtx, updatedTask.id, updatedTask.dueDateMillis ?: return@launch)
                Log.d(TAG, "Task updated and reminder rescheduled.")

                val personName = _householdMembers.value.find { it.id == currentPersonId }?.name ?: "Someone"

                //so completed tasks dont get fed twice
                if(pushToFeed) {
                    pushFeedEvent(EventType.MODIFIED, updatedTask, personName)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating task: ${e.message}", e)
                Toast.makeText(appCtx, "Failed to update task: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Deletes a task from the database.
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            Log.d(TAG, "Attempting to delete task: $task")
            try {
                ReminderScheduler.cancelReminder(appCtx, task.id)
                taskRepo.deleteTask(task)
                Log.d(TAG, "Task deleted and reminder removed.")

                val personName = _householdMembers.value.find { it.id == currentPersonId }?.name ?: "Someone"
                pushFeedEvent(EventType.DELETED, task, personName)

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task: ${e.message}", e)
                Toast.makeText(appCtx, "Failed to delete task: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val _allHouseholdTasks = MutableStateFlow<List<Task>>(emptyList())
    val allHouseholdTasks: StateFlow<List<Task>> = _allHouseholdTasks.asStateFlow()

    /**
     * Initializes the ViewModel to collect all household tasks.
     * It now syncs tasks from Firebase before collecting local data.
     */
    fun initializeForHousehold(householdName: String, householdEmail: String) {
        viewModelScope.launch {
            val household = householdDao.findByNameOrEmail(householdName, householdEmail)
            household?.let {
                currentHouseholdId = it.id

                // 1. Sync tasks from Firebase to Room before collecting local tasks
                syncTasksFromFirebaseToRoom(it)

                // gets feed updates from firebase
                if (!it.firebaseId.isNullOrBlank()) {
                    launch {
                        firebaseFeedRepo.listenToFeed(it.firebaseId!!).collectLatest { events ->
                            Log.d(TAG, "Realtime feed collected: ${events.size} items")
                            _feedEvents.value = events
                        }
                    }
                }

                // 2. Collect all tasks for the household (not just one person)
                launch {
                    taskRepo.getTasksForHousehold(it.id).collectLatest { taskList ->
                        _allHouseholdTasks.value = taskList
                    }
                }
                // Members as before
                launch {
                    taskRepo.getAllPeopleForHousehold(it.id).collectLatest { members ->
                        _householdMembers.value = members
                    }
                }
            }
        }
    }

    /**
     * Syncs tasks from Firebase to local Room database for the current household.
     * This is the version that prevents duplicates and does not access Room on the main thread.
     */
    fun syncTasksFromFirebaseToRoom(household: Household) {
        // Only sync if we have a firebaseId
        if (household.firebaseId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    taskRepo.syncTasksForHouseholdFromCloud(household)
                    Log.d(TAG, "Firebase-to-Room sync finished.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during Firebase-to-Room sync: ${e.message}", e)
                }
            }
        } else {
            Log.w(TAG, "Household firebaseId is null, skipping cloud sync.")
        }
    }

    fun markTaskCompleted(task: Task) {
        viewModelScope.launch {
            val completedTask = task.copy(status = TaskStatus.COMPLETED)
            updateTask(completedTask, false)

            val personName = _householdMembers.value.find { it.id == currentPersonId }?.name ?: "Someone"
            pushFeedEvent(EventType.COMPLETED, completedTask, personName)

        }
    }

    open fun setTaskStatusFilter(status: TaskStatus?) {
        _taskStatusFilter.value = status
    }
}