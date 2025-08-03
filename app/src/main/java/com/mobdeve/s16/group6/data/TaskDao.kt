package com.mobdeve.s16.group6.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE assigneeId = :personId AND householdId = :householdId ORDER BY dueDateMillis ASC, priority DESC")
    fun getTasksForPerson(personId: Int, householdId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE householdId = :householdId ORDER BY dueDateMillis ASC, priority DESC")
    fun getAllTasksForHousehold(householdId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getTaskByFirebaseId(firebaseId: String): Task?

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("SELECT * FROM tasks WHERE householdId = :householdId")
    fun getTasksForHousehold(householdId: Int): Flow<List<Task>>
}