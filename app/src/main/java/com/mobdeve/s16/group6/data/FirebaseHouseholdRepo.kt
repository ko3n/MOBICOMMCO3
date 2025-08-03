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
            e.printStackTrace()
            null
        }
    }

    suspend fun getHouseholdByNameOrEmail(name: String, email: String): Household? {
        return try {
            val snapshot = householdsRef.orderByChild("name").equalTo(name).get().await()
            for (child in snapshot.children) {
                val household = child.getValue(Household::class.java)
                if (household != null && (household.name == name || household.email == email)) {
                    household.firebaseId = child.key
                    return household
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}