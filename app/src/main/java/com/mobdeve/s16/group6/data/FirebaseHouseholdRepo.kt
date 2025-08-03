package com.mobdeve.s16.group6.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseHouseholdRepo {

    private val database: FirebaseDatabase = Firebase.database
    private val householdsRef: DatabaseReference = database.getReference("households")

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

}