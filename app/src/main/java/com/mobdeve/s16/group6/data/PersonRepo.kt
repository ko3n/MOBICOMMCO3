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
            Log.e(
                "PersonRepo",
                "Household not found for adding person: $householdName, $householdEmail"
            )
            return false
        }

        // Create Person object for Room
        val person = Person(name = personName, householdId = household.id)
        val result = personDao.insert(person) // Insert into Room
        if (result == -1L) {
            Log.d(
                "PersonRepo",
                "Person insertion into Room ignored (e.g., duplicate name in household)."
            )
            return false // Insertion was ignored due to conflict
        }
        Log.d("PersonRepo", "Person inserted into Room: ${person.name}")

        val localPerson = personDao.getPersonByNameAndHouseholdId(personName, household.id)
        if (localPerson != null) {
            // Sync to Firebase
            try {
                val firebaseId = firebasePersonRepo.addPerson(localPerson)
                if (firebaseId != null) {
                    localPerson.firebaseId = firebaseId
                    personDao.update(localPerson)
                    Log.d(
                        "PersonRepo",
                        "Person synced to Firebase and local record updated with ID: $firebaseId"
                    )
                } else {
                    Log.e(
                        "PersonRepo",
                        "Failed to get Firebase ID for new person. Local Room record not updated with Firebase ID."
                    )
                }
            } catch (e: Exception) {
                Log.e("PersonRepo", "Error syncing new person to Firebase: ${e.message}", e)
            }
        } else {
            Log.e(
                "PersonRepo",
                "Failed to retrieve local person after insertion for Firebase sync. Data might be inconsistent."
            )
        }

        return true
    }

    /**
     * Provides a Flow of lists of Person objects for a given household ID from Room.
     */
    fun getPeopleForCurrentHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }
}