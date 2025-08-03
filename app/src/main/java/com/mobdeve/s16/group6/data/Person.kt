package com.mobdeve.s16.group6.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "people",
    foreignKeys = [ForeignKey(
        entity = Household::class,
        parentColumns = ["id"],
        childColumns = ["householdId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [androidx.room.Index(value = ["name", "householdId"], unique = true)]
)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "householdId") var householdId: Int,
    var firebaseId: String? = null,
    var firebaseHouseholdId: String? = null
){
    constructor(): this(0, "", 0, null)
}