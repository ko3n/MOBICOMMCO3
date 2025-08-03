package com.mobdeve.s16.group6.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow

class PersonRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val personDao = db.personDao()
    private val householdDao = db.householdDao()
    private val firebasePersonRepo = FirebasePersonRepo()

    data class FirebasePersonDTO(
        val name: String = "",
        val householdId: String = "",  // Firebase householdId (String)
        val firebaseId: String? = null
    )

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
        // Save locally with local householdId
        val person = Person(name = personName, householdId = household.id)
        val result = personDao.insert(person)
        if (result == -1L) {
            Log.d("PersonRepo", "Person insertion into Room ignored (e.g., duplicate name).")
            return false
        }
        val localPerson = personDao.getPersonByNameAndHouseholdId(personName, household.id)
        if (localPerson != null && household.firebaseId != null) {
            try {
                // Upload with householdId set to firebaseId
                val firebasePerson = FirebasePersonDTO(
                    name = localPerson.name,
                    householdId = household.firebaseId!!,
                    firebaseId = localPerson.firebaseId
                )
                val firebaseId = firebasePersonRepo.addPerson(firebasePerson)
                if (firebaseId != null) {
                    localPerson.firebaseId = firebaseId
                    localPerson.firebaseHouseholdId = household.firebaseId
                    personDao.update(localPerson)
                }
            } catch (e: Exception) {
                Log.e("PersonRepo", "Error syncing new person to Firebase: ${e.message}", e)
            }
        }
        return true
    }

    fun getPeopleForCurrentHousehold(householdId: Int): Flow<List<Person>> {
        return personDao.getPeopleForHousehold(householdId)
    }

    suspend fun updatePerson(person: Person): Boolean {
        val success = firebasePersonRepo.updatePerson(person)
        if (success) {
            personDao.update(person)
            Log.d("PersonRepo", "Person updated successfully in Firebase and Room.")
        } else {
            Log.e("PersonRepo", "Failed to update person in Firebase.")
        }
        return success
    }

    suspend fun syncPeopleForHouseholdFromCloud(household: Household) {
        val peopleFromFirebase = firebasePersonRepo.getPeopleForHousehold(household.firebaseId ?: return)
        Log.d("PersonRepo", "Syncing people for household ${household.name} (firebaseId=${household.firebaseId})")
        for (personDto in peopleFromFirebase) {
            val firebaseHouseholdId = personDto.householdId
            val localHousehold = householdDao.findByFirebaseId(firebaseHouseholdId)
            val mappedHouseholdId = localHousehold?.id ?: household.id
            val roomPerson = Person(
                id = 0,
                name = personDto.name,
                householdId = mappedHouseholdId,
                firebaseId = personDto.firebaseId,
                firebaseHouseholdId = firebaseHouseholdId
            )
            val local = personDao.getPersonByNameAndHouseholdId(roomPerson.name, mappedHouseholdId)
            if (local == null) {
                personDao.insert(roomPerson)
            }
        }
    }
}