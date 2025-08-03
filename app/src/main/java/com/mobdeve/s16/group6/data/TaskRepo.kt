package com.mobdeve.s16.group6.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow

class TaskRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.taskDao()
    private val personDao = db.personDao()
    private val firebaseTaskRepo = FirebaseTaskRepo()

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

    fun getTasksForPerson(personId: Int, householdId: Int): Flow<List<Task>> {
        return taskDao.getTasksForPerson(personId, householdId)
    }

    fun getTasksForHousehold(householdId: Int): Flow<List<Task>> {
        return taskDao.getTasksForHousehold(householdId)
    }
    fun getAllPeopleForHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }
}