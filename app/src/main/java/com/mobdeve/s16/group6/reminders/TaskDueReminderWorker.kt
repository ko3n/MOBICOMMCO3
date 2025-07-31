package com.mobdeve.s16.group6.reminders

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobdeve.s16.group6.data.AppDatabase
import com.mobdeve.s16.group6.utils.NotificationUtils

class TaskDueReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt("taskId", -1)
        val reminderType = inputData.getString("reminderType") ?: "due"
        Log.d("ReminderWorker", "Running for taskId=$taskId, type=$reminderType")

        if (taskId == -1) {
            Log.e("ReminderWorker", "Invalid taskId")
            return Result.failure()
        }

        val taskDao = AppDatabase.getInstance(applicationContext).taskDao()
        val task = taskDao.getTaskById(taskId)
        if (task == null) {
            Log.e("ReminderWorker", "Task not found for ID=$taskId")
            return Result.retry()
        }

        Log.d("ReminderWorker", "Task loaded: ${task.title}")

        val (title, message) = when (reminderType) {
            "day_before" -> "Reminder: ${task.title} is due tomorrow" to "This task is due in 1 day."
            "due15s" -> "Reminder: ${task.title} is due in 15s" to "Only 15 seconds left!"
            else -> "Reminder: ${task.title}" to "This task is now due."
        }

        Log.d("ReminderWorker", "Sending notification: $title")
        NotificationUtils.showTaskNotification(
            context = applicationContext,
            title = title,
            message = message,
            notificationId = task.id
        )

        return Result.success()
    }

}
