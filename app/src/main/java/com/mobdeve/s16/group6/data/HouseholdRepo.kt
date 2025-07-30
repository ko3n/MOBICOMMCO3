package com.mobdeve.s16.group6.data

import android.content.Context
import android.util.Log

class HouseholdRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.householdDao()
    private val firebaseRepo = FirebaseHouseholdRepo()

    /**
     * Registers a new household.
     * Inserts into local Room database first, then attempts to sync to Firebase.
     * Updates the local record with the Firebase ID if sync is successful.
     */
    suspend fun register(name: String, email: String, password: String): RegistrationResult {
        if (dao.findByNameOrEmail(name, email) != null) {
            Log.d("HouseholdRepo", "Registration failed: Duplicate name or email.")
            return RegistrationResult.Duplicate
        }

        val household = Household(name = name, email = email, password = password)
        dao.insert(household)
        Log.d("HouseholdRepo", "Household inserted into Room: ${household.name}")

        val localHousehold = dao.findByNameOrEmail(name, email)
        if (localHousehold != null) {
            try {
                val firebaseId = firebaseRepo.registerHousehold(localHousehold)
                if (firebaseId != null) {
                    localHousehold.firebaseId = firebaseId
                    dao.update(localHousehold)
                    Log.d("HouseholdRepo", "Household synced to Firebase and local record updated with ID: $firebaseId")
                } else {
                    Log.e("HouseholdRepo", "Failed to get Firebase ID for new household. Local Room record not updated with Firebase ID.")
                }
            } catch (e: Exception) {
                Log.e("HouseholdRepo", "Error syncing new household to Firebase: ${e.message}", e)
            }
        } else {
            Log.e("HouseholdRepo", "Failed to retrieve local household after insertion for Firebase sync. Data might be inconsistent.")
        }

        return RegistrationResult.Success
    }

    /**
     * Authenticates a household against the local Room database.
     * Synchronization of authentication itself is usually handled by Firebase Auth,
     * but this method uses the local Room database for existing logic.
     */
    suspend fun authenticateAndGetHousehold(name: String, password: String): Household? {
        return dao.authenticate(name, password)
    }

    sealed class RegistrationResult {
        object Success : RegistrationResult()
        object Duplicate : RegistrationResult()
    }
}
