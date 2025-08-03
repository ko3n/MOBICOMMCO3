package com.mobdeve.s16.group6

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s16.group6.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch //important
import com.mobdeve.s16.group6.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    companion object {
        private const val TAG = "TaskViewModel"
    }

    /**
     * Initializes the ViewModel with the context of the current person and household.
     * This method must be called when navigating to the TaskScreen.
     * It fetches tasks for the given person and all household members for assignee selection.
     */
    fun initialize(personId: Int, householdName: String, householdEmail: String) {
        Log.d(TAG, "Initializing for personId: $personId, householdName: $householdName, householdEmail: $householdEmail")
        viewModelScope.launch {
            val household = householdDao.findByNameOrEmail(householdName, householdEmail)
            household?.let {
                currentHouseholdId = it.id
                currentPersonId = personId
                Log.d(TAG, "Household found: ID=${it.id}, Name=${it.name}. Starting data collection.")

                // Launch each collection in its own coroutine to allow concurrent collection
                // This fixes the issue where only the first collectLatest would run indefinitely.
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

            val newTask = Task(
                title = title,
                description = description,
                dueDateMillis = dueDateMillis,
                priority = priority,
                assigneeId = assigneeId,
                isRecurring = isRecurring,
                recurringInterval = recurringInterval,
                householdId = currentHouseholdId!!
            )
            Log.d(TAG, "Attempting to add task: $newTask")
            try {
                val taskId = taskRepo.addTask(newTask, firebaseHouseholdId).toInt() // <-- pass firebase id here!

                ReminderScheduler.scheduleFullReminderSet(appCtx, taskId, newTask.dueDateMillis ?: return@launch)
                Log.d(TAG, "Task added and reminder scheduled, taskID = $taskId.")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task: ${e.message}", e)
                Toast.makeText(appCtx, "Failed to add task: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Updates an existing task in the database.
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            Log.d(TAG, "Attempting to update task: $task")
            try {
                taskRepo.updateTask(task)
                ReminderScheduler.cancelReminder(appCtx, task.id)
                ReminderScheduler.scheduleFullReminderSet(appCtx, task.id, task.dueDateMillis ?: return@launch)
                Log.d(TAG, "Task updated and reminder rescheduled.")
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
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task: ${e.message}", e)
                Toast.makeText(appCtx, "Failed to delete task: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val _allHouseholdTasks = MutableStateFlow<List<Task>>(emptyList())
    val allHouseholdTasks: StateFlow<List<Task>> = _allHouseholdTasks.asStateFlow()

    fun initializeForHousehold(householdName: String, householdEmail: String) {
        viewModelScope.launch {
            val household = householdDao.findByNameOrEmail(householdName, householdEmail)
            household?.let {
                currentHouseholdId = it.id
                // Collect all tasks for the household (not just one person)
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

    fun markTaskCompleted(task: Task) {
        viewModelScope.launch {
            val completedTask = task.copy(status = TaskStatus.COMPLETED)
            updateTask(completedTask)
        }
    }

    open fun setTaskStatusFilter(status: TaskStatus?) {
        _taskStatusFilter.value = status
    }
}