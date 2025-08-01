package com.mobdeve.s16.group6.data

import java.util.*

fun calculateStatus(task: Task): TaskStatus {
    if (task.status == TaskStatus.COMPLETED) return TaskStatus.COMPLETED

    val dueMillis = task.dueDateMillis ?: return TaskStatus.UPCOMING
    val now = Calendar.getInstance().timeInMillis

    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val dueCal = Calendar.getInstance().apply { timeInMillis = dueMillis }
    val isDueToday =
        dueCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                dueCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

    return when {
        isDueToday -> TaskStatus.DUE_TODAY
        dueMillis < now -> TaskStatus.OVERDUE
        else -> TaskStatus.UPCOMING
    }
}