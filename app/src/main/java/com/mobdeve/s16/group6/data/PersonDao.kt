package com.mobdeve.s16.group6.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)

    @Query("SELECT * FROM people WHERE householdId = :householdId ORDER BY name ASC")
    fun getPeopleForHousehold(householdId: Int): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE name = :name AND householdId = :householdId LIMIT 1")
    suspend fun getPersonByNameAndHouseholdId(name: String, householdId: Int): Person?
}