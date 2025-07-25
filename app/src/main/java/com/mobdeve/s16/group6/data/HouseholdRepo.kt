package com.mobdeve.s16.group6.data

import android.content.Context

class HouseholdRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.householdDao()

    suspend fun register(name: String, email: String, password: String): RegistrationResult {
        if (dao.findByNameOrEmail(name, email) != null) {
            return RegistrationResult.Duplicate
        }
        val household = Household(name = name, email = email, password = password)
        dao.insert(household)
        return RegistrationResult.Success
    }

    suspend fun authenticateAndGetHousehold(name: String, password: String): Household? {
        return dao.authenticate(name, password)
    }

    sealed class RegistrationResult {
        object Success : RegistrationResult()
        object Duplicate : RegistrationResult()
    }
}