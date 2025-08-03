package com.mobdeve.s16.group6.data

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseTaskRepo(context: Context) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val tasksRef: DatabaseReference = database.getReference("tasks")

    suspend fun addTask(firebaseTask: TaskRepo.FirebaseTaskDTO): String? {
        return try {
            val newTaskRef = tasksRef.push()
            val firebaseId = newTaskRef.key
            if (firebaseId != null) {
                val taskForUpload = firebaseTask.copy(firebaseId = firebaseId)
                newTaskRef.setValue(taskForUpload).await()
            }
            firebaseId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateTask(task: Task): Boolean {
        return try {
            if (task.firebaseId == null) {
                return false
            }
            tasksRef.child(task.firebaseId!!).setValue(task).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTask(firebaseId: String) {
        try {
            tasksRef.child(firebaseId).removeValue().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTasksForHousehold(householdId: String): List<TaskRepo.FirebaseTaskDTO> {
        return try {
            val snapshot = tasksRef.orderByChild("householdId").equalTo(householdId).get().await()
            val tasksList = mutableListOf<TaskRepo.FirebaseTaskDTO>()
            for (child in snapshot.children) {
                val dto = child.getValue(TaskRepo.FirebaseTaskDTO::class.java)
                if (dto != null) {
                    tasksList.add(dto.copy(firebaseId = child.key))
                }
            }
            tasksList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}