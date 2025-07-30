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
 * Repository for interacting with Firebase Realtime Database for Person data.
 * This class handles all Firebase-specific operations for people within households.
 */
class FirebasePersonRepo {

    private val database: FirebaseDatabase = Firebase.database
    private val peopleRef: DatabaseReference = database.getReference("people")

    /**
     * Adds a new person to a specific household in Firebase Realtime Database.
     * Generates a new Firebase key and sets it in the Person object before saving.
     *
     * @param person The Person object to add. Must have a valid householdId.
     * @return The Firebase ID of the newly added person, or null if an error occurred.
     */
    suspend fun addPerson(person: Person): String? {
        return try {
            val newPersonRef = peopleRef.push()
            val firebaseId = newPersonRef.key
            if (firebaseId != null) {
                person.firebaseId = firebaseId
                newPersonRef.setValue(person).await()
            }
            firebaseId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Provides a Flow of lists of Person objects for a given household ID.
     * This will emit updates whenever the data changes in Firebase.
     *
     * @param householdId The ID of the household to fetch people for.
     * @return A Flow emitting lists of Person objects.
     */
    fun getPeopleForHousehold(householdId: Int): Flow<List<Person>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val peopleList = mutableListOf<Person>()
                for (childSnapshot in snapshot.children) {
                    val person = childSnapshot.getValue(Person::class.java)
                    if (person != null) {
                        peopleList.add(person)
                    }
                }
                trySend(peopleList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        // Attach the listener to the specific household's people
        // Assuming people are stored under "people" and filtered by householdId
        peopleRef.orderByChild("householdId").equalTo(householdId.toDouble()).addValueEventListener(listener)

        // Remove the listener when the flow is cancelled or completes
        awaitClose {
            peopleRef.orderByChild("householdId").equalTo(householdId.toDouble()).removeEventListener(listener)
        }
    }

    /**
     * Updates an existing person in Firebase Realtime Database.
     *
     * @param person The Person object to update. Must have a non-null firebaseId.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updatePerson(person: Person): Boolean {
        return try {
            if (person.firebaseId == null) {
                return false // Cannot update without a Firebase ID
            }
            peopleRef.child(person.firebaseId!!).setValue(person).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes a person from Firebase Realtime Database.
     *
     * @param firebaseId The Firebase ID of the person to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deletePerson(firebaseId: String): Boolean {
        return try {
            peopleRef.child(firebaseId).removeValue().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}