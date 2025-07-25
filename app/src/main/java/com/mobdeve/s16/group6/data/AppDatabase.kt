package com.mobdeve.s16.group6.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Household::class, Person::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun householdDao(): HouseholdDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "choreo-db"
                )
                    .fallbackToDestructiveMigration() // (ONLY FOR DEVELOPMENT)
                    .build().also { INSTANCE = it }
            }
    }
}