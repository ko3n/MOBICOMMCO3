package com.mobdeve.s16.group6.reminders

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import com.mobdeve.s16.group6.utils.NotificationUtils
import java.text.SimpleDateFormat
import com.mobdeve.s16.group6.utils.SettingsManager
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(context: Context, taskId: Int, dueTimeMillis: Long, suffix: String = "") {
        val now = System.currentTimeMillis()
        val delay = dueTimeMillis - now

        //debug
        val mins = delay / 60000
        val formattedDue = SimpleDateFormat("MMM dd h:mm a", Locale.getDefault()).format(
            Date(
                dueTimeMillis
            )
        )
        val formattedNow = SimpleDateFormat("MMM dd h:mm a", Locale.getDefault()).format(Date(now))
        Log.d("ReminderScheduler", "Scheduling '$suffix' for taskId=$taskId")
        Log.d("ReminderScheduler", "Now: $formattedNow, Due: $formattedDue, Delay: $delay ms ($mins min)")


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
        val settingsManager = SettingsManager(context)
        if (!settingsManager.getNotificationPreference()) {

            Log.d("ReminderScheduler", "Notifications are disabled in settings. Skipping reminder scheduling for task ID $taskId.")
            return
        }
        Log.d("ReminderScheduler", "Notifications are enabled. Proceeding with scheduling for task ID $taskId.")
        //debug reminder
//        scheduleReminder(context, taskId, System.currentTimeMillis() + 15_000L, "due15s")
        scheduleReminder(context, taskId, dueTimeMillis, "due")
        scheduleReminder(context, taskId, dueTimeMillis - TimeUnit.DAYS.toMillis(1), "day_before")
    }

    fun cancelReminder(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_task_${taskId}_due")
        WorkManager.getInstance(context).cancelUniqueWork("reminder_task_${taskId}_day_before")
    }
}