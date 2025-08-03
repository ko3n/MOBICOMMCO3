package com.mobdeve.s16.group6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s16.group6.data.Household
import com.mobdeve.s16.group6.data.HouseholdRepo
import com.mobdeve.s16.group6.data.PersonRepo
import com.mobdeve.s16.group6.data.TaskRepo
import com.mobdeve.s16.group6.utils.PasswordValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val householdRepo = HouseholdRepo(application)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _loginErrorMessage = MutableStateFlow<String?>(null)
    val loginErrorMessage: StateFlow<String?> = _loginErrorMessage.asStateFlow()

    private val _signupErrorMessage = MutableStateFlow<String?>(null)
    val signupErrorMessage: StateFlow<String?> = _signupErrorMessage.asStateFlow()

    private val _currentHousehold = MutableStateFlow<Household?>(null)
    val currentHousehold: StateFlow<Household?> = _currentHousehold.asStateFlow()

    fun login(householdName: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginErrorMessage.value = null
            var household = householdRepo.authenticateAndGetHousehold(householdName, password)

            if (household != null) {
                val updatedHousehold = HouseholdRepo(getApplication()).getLocalHouseholdByNameOrEmail(household.name, household.email) ?: household

                val personRepo = PersonRepo(getApplication())
                val taskRepo = TaskRepo(getApplication())
                personRepo.syncPeopleForHouseholdFromCloud(updatedHousehold)
                taskRepo.syncTasksForHouseholdFromCloud(updatedHousehold)

                _isAuthenticated.value = true
                _currentHousehold.value = updatedHousehold
                onComplete(true)
            } else {
                _loginErrorMessage.value = "Invalid household name or password."
                _isAuthenticated.value = false
                _currentHousehold.value = null
                onComplete(false)
            }
        }
    }

    fun register(householdName: String, email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _signupErrorMessage.value = null

            if (!PasswordValidator.isValid(password)) {
                _signupErrorMessage.value = "Password must be at least 8 characters long, contain at least 1 number, 1 uppercase & 1 lowercase letter."
                onComplete(false)
                return@launch
            }

            when (householdRepo.register(householdName, email, password)) {
                HouseholdRepo.RegistrationResult.Success -> {
                    val household = householdRepo.authenticateAndGetHousehold(householdName, password)
                    if (household != null) {
                        _isAuthenticated.value = true
                        _currentHousehold.value = household
                        onComplete(true)
                    } else {
                        _signupErrorMessage.value = "Registration successful but failed to log in automatically."
                        onComplete(false)
                    }
                }
                HouseholdRepo.RegistrationResult.Duplicate -> {
                    _signupErrorMessage.value = "Household name or email already taken."
                    _isAuthenticated.value = false
                    _currentHousehold.value = null
                    onComplete(false)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isAuthenticated.value = false
            _currentHousehold.value = null
        }
    }
    fun setSignupError(message: String) {
        _signupErrorMessage.value = message
    }

    fun clearLoginError() {
        _loginErrorMessage.value = null
    }

    fun clearSignupError() {
        _signupErrorMessage.value = null
    }
}