package com.mobdeve.s16.group6.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * Repository for interacting with Firebase Realtime Database for Task data.
 * This class handles all Firebase-specific operations for tasks.
 */
class FirebaseTaskRepo {

    private val database: FirebaseDatabase = Firebase.database
    private val tasksRef: DatabaseReference = database.getReference("tasks")

    /**
     * Adds a new task to Firebase Realtime Database.
     * Generates a new Firebase key and sets it in the Task object before saving.
     *
     * @param task The Task object to add.
     * @return The Firebase ID of the newly added task, or null if an error occurred.
     */
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

    /**
     * Provides a Flow of lists of Task objects for a specific person and household.
     * This will emit updates whenever the data changes in Firebase.
     *
     * @param personId The ID of the assignee (person) to fetch tasks for.
     * @param householdId The ID of the household the tasks belong to.
     * @return A Flow emitting lists of Task objects.
     */
    fun getTasksForPerson(personId: Int, householdId: Int): Flow<List<Task>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasksList = mutableListOf<Task>()
                for (childSnapshot in snapshot.children) {
                    val task = childSnapshot.getValue(Task::class.java)
                    // Filter tasks by assigneeId and householdId
                    if (task != null && task.assigneeId == personId && task.householdId == householdId) {
                        tasksList.add(task)
                    }
                }
                trySend(tasksList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        // Attach the listener to the tasks node and filter client-side or with more specific queries if possible
        // For complex queries (multiple fields), client-side filtering after fetching is often needed with Realtime DB
        tasksRef.orderByChild("householdId").equalTo(householdId.toDouble()).addValueEventListener(listener)

        awaitClose {
            tasksRef.orderByChild("householdId").equalTo(householdId.toDouble()).removeEventListener(listener)
        }
    }

    /**
     * Provides a Flow of lists of all Task objects for a given household ID.
     * This will emit updates whenever the data changes in Firebase.
     *
     * @param householdId The ID of the household to fetch all tasks for.
     * @return A Flow emitting lists of Task objects.
     */
    fun getAllTasksForHousehold(householdId: Int): Flow<List<Task>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasksList = mutableListOf<Task>()
                for (childSnapshot in snapshot.children) {
                    val task = childSnapshot.getValue(Task::class.java)
                    if (task != null && task.householdId == householdId) {
                        tasksList.add(task)
                    }
                }
                trySend(tasksList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        tasksRef.orderByChild("householdId").equalTo(householdId.toDouble()).addValueEventListener(listener)

        awaitClose {
            tasksRef.orderByChild("householdId").equalTo(householdId.toDouble()).removeEventListener(listener)
        }
    }

    /**
     * Updates an existing task in Firebase Realtime Database.
     *
     * @param task The Task object to update. Must have a non-null firebaseId.
     * @return True if the update was successful, false otherwise.
     */
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

    /**
     * Deletes a task from Firebase Realtime Database.
     *
     * @param firebaseId The Firebase ID of the task to delete.
     * @return True if the deletion was successful, false otherwise.
     */
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