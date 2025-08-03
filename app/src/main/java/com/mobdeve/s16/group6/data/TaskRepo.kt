package com.mobdeve.s16.group6.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow

class TaskRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.taskDao()
    private val personDao = db.personDao()
    private val firebaseTaskRepo = FirebaseTaskRepo()

    /**
     * Adds a new task.
     * Inserts into local Room database first, then attempts to sync to Firebase.
     * Updates the local record with the Firebase ID if sync is successful.
     */
    suspend fun addTask(task: Task): Long {
        val finalTask = task.copy(status = calculateStatus(task))
        val roomId = taskDao.insert(finalTask)
        Log.d("TaskRepo", "Task inserted into Room with ID: $roomId")

        val localTask = taskDao.getTaskById(roomId.toInt())
        if (localTask != null) {
            // Sync to Firebase
            try {
                val firebaseId = firebaseTaskRepo.addTask(localTask)
                if (firebaseId != null) {

                    localTask.firebaseId = firebaseId
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

    /**
     * Updates an existing task.
     * Updates local Room database, then attempts to sync to Firebase.
     */
    suspend fun updateTask(task: Task) {
        val updatedTask = if (task.status != TaskStatus.COMPLETED)
            task.copy(status = calculateStatus(task))
        else task

        try {
            taskDao.update(updatedTask)
            Log.d("TaskRepo", "Task updated in Room: ${updatedTask.title}")

            if (updatedTask.firebaseId != null) {
                firebaseTaskRepo.updateTask(updatedTask)
                Log.d("TaskRepo", "Task updated in Firebase: ${updatedTask.firebaseId}")
            } else {
                Log.w("TaskRepo", "Task has no Firebase ID, skipping Firebase update.")
            }
        } catch (e: Exception) {
            Log.e("TaskRepo", "Error updating task locally or in Firebase: ${e.message}", e)
        }
    }

    /**
     * Deletes a task.
     * Deletes from local Room database, then attempts to sync to Firebase.
     */
    suspend fun deleteTask(task: Task) {
        try {
            taskDao.delete(task)
            Log.d("TaskRepo", "Task deleted from Room: ${task.title}")

            if (task.firebaseId != null) {
                firebaseTaskRepo.deleteTask(task.firebaseId!!)
                Log.d("TaskRepo", "Task deleted from Firebase: ${task.firebaseId}")
            } else {
                Log.w("TaskRepo", "Task has no Firebase ID, skipping Firebase delete.")
            }
        } catch (e: Exception) {
            Log.e("TaskRepo", "Error deleting task locally or in Firebase: ${e.message}", e)
        }
    }

    /**
     * Provides a Flow of lists of Task objects for a specific person and household from Room.
     */
    fun getTasksForPerson(personId: Int, householdId: Int): Flow<List<Task>> {
        return taskDao.getTasksForPerson(personId, householdId)
    }

    /**
     * Provides a Flow of lists of all Task objects for a specific household from Room.
     */
    fun getTasksForHousehold(householdId: Int): Flow<List<Task>> {
        return taskDao.getTasksForHousehold(householdId)
    }

    /**
     * Provides a Flow of lists of all Person objects for a given household ID from Room.
     * (This method seems to be misplaced in TaskRepo, but keeping it as per original for now)
     */
    fun getAllPeopleForHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }
}