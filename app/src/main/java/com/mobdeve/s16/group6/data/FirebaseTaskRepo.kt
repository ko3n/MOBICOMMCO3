package com.mobdeve.s16.group6.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseTaskRepo {

    private val database: FirebaseDatabase = Firebase.database
    private val tasksRef: DatabaseReference = database.getReference("tasks")

    suspend fun addTask(task: Task): String? {
        return try {
            val newTaskRef = tasksRef.push()
            val firebaseId = newTaskRef.key
            if (firebaseId != null) {
                task.firebaseId = firebaseId
                newTaskRef.setValue(task).await()
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
                return false // Cannot update without a Firebase ID
            }
            tasksRef.child(task.firebaseId!!).setValue(task).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTask(firebaseId: String): Boolean {
        return try {
            tasksRef.child(firebaseId).removeValue().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}