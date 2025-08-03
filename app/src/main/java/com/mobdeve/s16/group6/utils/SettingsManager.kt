package com.mobdeve.s16.group6.utils

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    /**
     * Saves the user's preference for notifications.
     */
    fun saveNotificationPreference(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    /**
     * Retrieves the user's saved preference for notifications.
     * Defaults to true (on) if no preference has been saved yet.
     */
    fun getNotificationPreference(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
}