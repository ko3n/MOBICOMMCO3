package com.mobdeve.s16.group6.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["assigneeId"],
            onDelete = ForeignKey.SET_NULL // When person is deleted, assigneeId becomes NULL
        ),
        ForeignKey(
            entity = Household::class,
            parentColumns = ["id"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.CASCADE // Delete tasks if household is deleted
        )
    ]
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
    val householdId: Int // Link to the Household
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class RecurringInterval {
    DAILY, WEEKLY, MONTHLY, YEARLY
}