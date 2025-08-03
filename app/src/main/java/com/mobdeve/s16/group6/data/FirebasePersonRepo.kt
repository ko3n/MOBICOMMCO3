package com.mobdeve.s16.group6.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class FirebasePersonRepo {

    private val database: FirebaseDatabase = Firebase.database
    private val peopleRef: DatabaseReference = database.getReference("people")

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

    suspend fun getPeopleForHousehold(householdId: String): List<Person> {
        return try {
            val snapshot = peopleRef.orderByChild("householdId").equalTo(householdId).get().await()
            val peopleList = mutableListOf<Person>()
            for (child in snapshot.children) {
                val person = child.getValue(Person::class.java)
                if (person != null) {
                    person.firebaseId = child.key
                    peopleList.add(person)
                }
            }
            peopleList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}