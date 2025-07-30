package com.mobdeve.s16.group6.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Repository for interacting with Firebase Realtime Database for Household data.
 * This class handles all Firebase-specific operations for households.
 */
class FirebaseHouseholdRepo {

    private val database: FirebaseDatabase = Firebase.database
    private val householdsRef: DatabaseReference = database.getReference("households")

    /**
     * Registers a new household in Firebase Realtime Database.
     * Generates a new Firebase key and sets it in the Household object before saving.
     *
     * @param household The Household object to register.
     * @return The Firebase ID of the newly registered household, or null if an error occurred.
     */
    suspend fun registerHousehold(household: Household): String? {
        return try {
            val newHouseholdRef = householdsRef.push()
            val firebaseId = newHouseholdRef.key
            if (firebaseId != null) {
                household.firebaseId = firebaseId
                newHouseholdRef.setValue(household).await()
            }
            firebaseId
        } catch (e: Exception) {
            // Log the exception or handle it as needed
            e.printStackTrace()
            null
        }
    }

    /**
     * Authenticates a household by checking name and password against Firebase.
     * Note: For production, passwords should be hashed and compared securely.
     * This is a simplified example for demonstration.
     *
     * @param name The name of the household.
     * @param password The password of the household.
     * @return The Household object if authentication is successful, otherwise null.
     */
    suspend fun authenticateHousehold(name: String, password: String): Household? {
        return try {
            val snapshot = householdsRef.orderByChild("name").equalTo(name).get().await()
            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    val household = childSnapshot.getValue(Household::class.java)
                    if (household != null && household.password == password) {
                        return household
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Finds a household by its Firebase ID.
     *
     * @param firebaseId The Firebase ID of the household.
     * @return The Household object if found, otherwise null.
     */
    suspend fun getHouseholdByFirebaseId(firebaseId: String): Household? {
        return try {
            val snapshot = householdsRef.child(firebaseId).get().await()
            snapshot.getValue(Household::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Updates an existing household in Firebase Realtime Database.
     *
     * @param household The Household object to update. Must have a non-null firebaseId.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateHousehold(household: Household): Boolean {
        return try {
            if (household.firebaseId == null) {
                return false // Cannot update without a Firebase ID
            }
            householdsRef.child(household.firebaseId!!).setValue(household).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes a household from Firebase Realtime Database.
     *
     * @param firebaseId The Firebase ID of the household to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deleteHousehold(firebaseId: String): Boolean {
        return try {
            householdsRef.child(firebaseId).removeValue().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}