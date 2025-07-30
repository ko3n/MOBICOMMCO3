package com.mobdeve.s16.group6.reminders

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(context: Context, taskId: Int, dueTimeMillis: Long, suffix: String = "") {
        val now = System.currentTimeMillis()
        val delay = dueTimeMillis - now

        if (delay <= 0) return

        val workData = Data.Builder()
            .putInt("taskId", taskId)
            .putString("reminderType", suffix)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TaskDueReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .build()

        val uniqueName = "reminder_task_${taskId}_$suffix"
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun scheduleFullReminderSet(context: Context, taskId: Int, dueTimeMillis: Long) {
        scheduleReminder(context, taskId, dueTimeMillis, "due")
        scheduleReminder(context, taskId, dueTimeMillis - TimeUnit.DAYS.toMillis(1), "day_before")
    }

    fun cancelReminder(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_task_${taskId}_due")
        WorkManager.getInstance(context).cancelUniqueWork("reminder_task_${taskId}_day_before")
    }
}
