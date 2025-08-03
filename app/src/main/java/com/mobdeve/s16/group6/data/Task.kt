package com.mobdeve.s16.group6.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = Household::class,
        parentColumns = ["id"],
        childColumns = ["householdId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [androidx.room.Index(value = ["householdId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val dueDateMillis: Long? = null,
    val priority: TaskPriority = TaskPriority.LOW,
    val assigneeId: Int? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval? = null,
    @ColumnInfo(name = "householdId") var householdId: Int,
    var firebaseId: String? = null,
    var firebaseHouseholdId: String? = null,
    val status: TaskStatus = TaskStatus.UPCOMING
){
    constructor(): this(0,"",null,null,TaskPriority.LOW,null,false,null,0,null,null,TaskStatus.UPCOMING)
}

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class RecurringInterval {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

enum class TaskStatus {
    UPCOMING,
    COMPLETED,
    DUE_TODAY,
    OVERDUE
}