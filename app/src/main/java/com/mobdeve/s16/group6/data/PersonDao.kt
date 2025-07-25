package com.mobdeve.s16.group6.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // IGNORE will simply not insert if conflict (e.g., non-unique name)
    suspend fun insert(person: Person): Long // Return rowId or -1 if ignored

    @Query("SELECT * FROM people WHERE householdId = :householdId ORDER BY name ASC")
    fun getPeopleForHousehold(householdId: Int): Flow<List<Person>> // Use Flow for observing changes
}