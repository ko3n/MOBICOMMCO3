package com.mobdeve.s16.group6.data

import androidx.room.*

@Dao
interface HouseholdDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(household: Household)

    @Query("SELECT * FROM households WHERE name = :name OR email = :email LIMIT 1")
    suspend fun findByNameOrEmail(name: String, email: String): Household?

    @Query("SELECT * FROM households WHERE name = :name AND password = :password LIMIT 1")
    suspend fun authenticate(name: String, password: String): Household?
}