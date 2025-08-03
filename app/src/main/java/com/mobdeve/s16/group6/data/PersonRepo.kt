package com.mobdeve.s16.group6.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow

class PersonRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val personDao = db.personDao()
    private val householdDao = db.householdDao()
    private val firebasePersonRepo = FirebasePersonRepo() // Instantiate Firebase Person repo

    /**
     * Adds a new person.
     * Inserts into local Room database first, then attempts to sync to Firebase.
     * Updates the local record with the Firebase ID if sync is successful.
     */
    suspend fun addPerson(
        personName: String,
        householdName: String,
        householdEmail: String
    ): Boolean {
        val household = householdDao.findByNameOrEmail(householdName, householdEmail)
        if (household == null) {
            Log.e("PersonRepo", "Household not found for adding person: $householdName, $householdEmail")
            return false
        }

        val person = Person(name = personName, householdId = household.id)
        val result = personDao.insert(person)
        if (result == -1L) {
            Log.d("PersonRepo", "Person insertion into Room ignored (e.g., duplicate name).")
            return false
        }

        val localPerson = personDao.getPersonByNameAndHouseholdId(personName, household.id)
        if (localPerson != null) {
            try {
                val firebaseId = firebasePersonRepo.addPerson(localPerson)
                if (firebaseId != null) {
                    localPerson.firebaseId = firebaseId
                    personDao.update(localPerson)
                }
            } catch (e: Exception) {
                Log.e("PersonRepo", "Error syncing new person to Firebase: ${e.message}", e)
            }
        }
        return true
    }

    /**
     * Provides a Flow of lists of Person objects for a given household ID from Room.
     */
    fun getPeopleForCurrentHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }

    /**
     * Updates a person's details in both Firebase and the local Room database.
     * @param person The person object with updated information.
     * @return True if the update was successful in Firebase.
     */
    suspend fun updatePerson(person: Person): Boolean {
        // First, update the record in Firebase.
        val success = firebasePersonRepo.updatePerson(person)

        // If the Firebase update was successful, update the local database too.
        if (success) {
            personDao.update(person)
            Log.d("PersonRepo", "Person updated successfully in Firebase and Room.")
        } else {
            Log.e("PersonRepo", "Failed to update person in Firebase.")
        }
        return success
    }
}