package com.mobdeve.s16.group6.utils

object PasswordValidator {
    // At least 8 chars, at least one uppercase, one lowercase, one number or special, only allowed characters
    private val PASSWORD_REGEX = Regex(
        """^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9!@#\$%^&*()_\-+=\[\]{};':",.<>?/\\|`~])([A-Za-z0-9!@#\$%^&*()_\-+=\[\]{};':",.<>?/\\|`~]{8,})$"""
    )

    fun isValid(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }
}