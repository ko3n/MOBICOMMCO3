package com.mobdeve.s16.group6.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "households",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class Household(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String, // need to hash
    var firebaseId: String? = null
)