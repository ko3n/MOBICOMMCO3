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

//    override suspend fun doWork(): Result {
//        val taskId = inputData.getInt("taskId", -1)
//        if (taskId == -1) return Result.failure()
//
//        val taskDao = AppDatabase.getInstance(applicationContext).taskDao()
//        val task = taskDao.getTaskById(taskId) ?: return Result.failure()
//
//        val reminderType = inputData.getString("reminderType") ?: "due"
//        val title = when (reminderType) {
//            "day_before" -> "Reminder: ${task.title} is due tomorrow"
//            else -> "Reminder: ${task.title}"
//        }
//        val message = when (reminderType) {
//            "day_before" -> "This task is due in 1 day."
//            else -> "This task is now due."
//        }
//
//        NotificationUtils.showTaskNotification(
//            context = applicationContext,
//            title = title,
//            message = message,
//            notificationId = task.id
//        )
//
//        return Result.success()
//    }
    //testing
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
            return Result.failure()
        }

        Log.d("ReminderWorker", "Task loaded: ${task.title}")

        val (title, message) = when (reminderType) {
            "day_before" -> "Reminder: ${task.title} is due tomorrow" to "This task is due in 1 day."
            "15min_before" -> "Reminder: ${task.title} is due soon" to "Only 15 minutes left!"
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
