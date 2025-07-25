package com.mobdeve.s16.group6.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class PersonRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val personDao = db.personDao()
    private val householdDao = db.householdDao()

    suspend fun addPerson(personName: String, householdName: String, householdEmail: String): Boolean {
        // First, find the household to get its ID
        val household = householdDao.findByNameOrEmail(householdName, householdEmail)
        if (household == null) {
            return false
        }

        val person = Person(name = personName, householdId = household.id)
        val result = personDao.insert(person)
        return result != -1L // Returns true if insertion was successful (not ignored)
    }

    fun getPeopleForCurrentHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }
}