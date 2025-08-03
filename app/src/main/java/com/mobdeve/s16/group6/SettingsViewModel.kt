package com.mobdeve.s16.group6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s16.group6.data.TaskRepo // Make sure this is imported
import com.mobdeve.s16.group6.reminders.ReminderScheduler // Make sure this is imported
import com.mobdeve.s16.group6.utils.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)
    private val taskRepo = TaskRepo(application) // Get an instance of TaskRepo
    private val appCtx = application // Get the context for the scheduler

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // NEW: We need to know the current household to cancel tasks for
    private var currentHouseholdId: Int? = null

    init {
        loadInitialSettings()
    }

    // NEW: This function must be called from MainActivity so the ViewModel knows which household it's working with.
    fun initialize(householdId: Int) {
        currentHouseholdId = householdId
    }

    private fun loadInitialSettings() {
        _notificationsEnabled.value = settingsManager.getNotificationPreference()
    }

    /**
     * This function is called by the UI when the user taps the Notifications switch.
     */
    fun onNotificationsToggled(isEnabled: Boolean) {
        _notificationsEnabled.value = isEnabled
        settingsManager.saveNotificationPreference(isEnabled)

//        if (!isEnabled) {
//            cancelAllScheduledReminders()
//        }
    }

    private fun cancelAllScheduledReminders() {
        viewModelScope.launch {
            // Only proceed if we know which household we're in
            currentHouseholdId?.let { hhId ->
                // 1. Get all upcoming tasks from the repository for the current household.
                val upcomingTasks = taskRepo.getAllIncompleteTasksForHousehold(hhId)

                // 2. Loop through each task and tell the ReminderScheduler to cancel its specific alarms.
                for (task in upcomingTasks) {
                    ReminderScheduler.cancelReminder(appCtx, task.id)
                }
            }
        }
    }
}