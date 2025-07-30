package com.mobdeve.s16.group6.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class TaskRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.taskDao()
    private val personDao = db.personDao()

    suspend fun addTask(task: Task): Long {
        return taskDao.insert(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.update(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    fun getTasksForPerson(personId: Int, householdId: Int): Flow<List<Task>> {
        return taskDao.getTasksForPerson(personId, householdId)
    }

    fun getAllPeopleForHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }
}