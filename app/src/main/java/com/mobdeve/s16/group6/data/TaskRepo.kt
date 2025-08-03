package com.mobdeve.s16.group6.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow

class TaskRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.taskDao()
    private val personDao = db.personDao()
    private val householdDao = db.householdDao()
    private val firebaseTaskRepo = FirebaseTaskRepo(context)

    data class FirebaseTaskDTO(
        val title: String = "",
        val description: String? = null,
        val dueDateMillis: Long? = null,
        val priority: TaskPriority = TaskPriority.LOW,
        val assigneeId: Int? = null,
        val isRecurring: Boolean = false,
        val recurringInterval: RecurringInterval? = null,
        val householdId: String = "", // Firebase householdId (String)
        val firebaseId: String? = null,
        val status: TaskStatus = TaskStatus.UPCOMING
    )

    // Local Room queries
    fun getTasksForPerson(personId: Int, householdId: Int): Flow<List<Task>> {
        return taskDao.getTasksForPerson(personId, householdId)
    }

    fun getTasksForHousehold(householdId: Int): Flow<List<Task>> {
        return taskDao.getAllTasksForHousehold(householdId)
    }

    fun getAllPeopleForHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }

    // Add a new task (local + cloud)
    suspend fun addTask(task: Task, householdFirebaseId: String): Long {
        val finalTask = task.copy(status = calculateStatus(task))
        val roomId = taskDao.insert(finalTask)
        Log.d("TaskRepo", "Task inserted into Room with ID: $roomId")

        val localTask = taskDao.getTaskById(roomId.toInt())
        if (localTask != null) {
            try {
                val firebaseTask = FirebaseTaskDTO(
                    title = localTask.title,
                    description = localTask.description,
                    dueDateMillis = localTask.dueDateMillis,
                    priority = localTask.priority,
                    assigneeId = localTask.assigneeId,
                    isRecurring = localTask.isRecurring,
                    recurringInterval = localTask.recurringInterval,
                    householdId = householdFirebaseId,
                    firebaseId = localTask.firebaseId,
                    status = localTask.status
                )
                val firebaseId = firebaseTaskRepo.addTask(firebaseTask)
                if (firebaseId != null) {
                    localTask.firebaseId = firebaseId
                    localTask.firebaseHouseholdId = householdFirebaseId
                    taskDao.update(localTask)
                    Log.d("TaskRepo", "Task synced to Firebase and local record updated with ID: $firebaseId")
                } else {
                    Log.e("TaskRepo", "Failed to get Firebase ID for new task. Local Room record not updated with Firebase ID.")
                }
            } catch (e: Exception) {
                Log.e("TaskRepo", "Error syncing new task to Firebase: ${e.message}", e)
            }
        } else {
            Log.e("TaskRepo", "Failed to retrieve local task after insertion for Firebase sync. Data might be inconsistent.")
        }
        return roomId
    }

    // Update a task (local+cloud)
    suspend fun updateTask(task: Task) {
        val success = firebaseTaskRepo.updateTask(task)
        if (success) {
            taskDao.update(task)
            Log.d("TaskRepo", "Task updated in Firebase and Room.")
        } else {
            Log.e("TaskRepo", "Failed to update task in Firebase.")
        }
    }

    // Delete a task (local+cloud)
    suspend fun deleteTask(task: Task) {
        val firebaseId = task.firebaseId
        if (firebaseId != null) {
            try {
                firebaseTaskRepo.deleteTask(firebaseId)
            } catch (e: Exception) {
                Log.e("TaskRepo", "Error deleting task from Firebase: ${e.message}", e)
            }
        }
        taskDao.delete(task)
    }

    // Sync tasks from Firebase to Room for a household
    suspend fun syncTasksForHouseholdFromCloud(household: Household) {
        val tasksFromFirebase = firebaseTaskRepo.getTasksForHousehold(household.firebaseId ?: return)
        for (dto in tasksFromFirebase) {
            val localHousehold = householdDao.findByFirebaseId(dto.householdId)
            val mappedHouseholdId = localHousehold?.id ?: household.id
            val roomTask = Task(
                id = 0, // Room will auto-assign ID if inserting
                title = dto.title,
                description = dto.description,
                dueDateMillis = dto.dueDateMillis,
                priority = dto.priority,
                assigneeId = dto.assigneeId,
                isRecurring = dto.isRecurring,
                recurringInterval = dto.recurringInterval,
                householdId = mappedHouseholdId,
                firebaseId = dto.firebaseId,
                firebaseHouseholdId = dto.householdId,
                status = dto.status
            )
            val local = taskDao.getTaskByFirebaseId(dto.firebaseId ?: "")
            if (local == null) {
                // Only insert if not present
                taskDao.insert(roomTask)
            } else {
                val updatedTask = local.copy(
                    title = dto.title,
                    description = dto.description,
                    dueDateMillis = dto.dueDateMillis,
                    priority = dto.priority,
                    assigneeId = dto.assigneeId,
                    isRecurring = dto.isRecurring,
                    recurringInterval = dto.recurringInterval,
                    status = dto.status
                )
                taskDao.update(updatedTask)
            }
        }
    }
}