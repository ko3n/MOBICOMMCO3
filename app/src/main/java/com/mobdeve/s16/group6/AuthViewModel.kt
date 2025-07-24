package com.mobdeve.s16.group6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s16.group6.data.HouseholdRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HouseholdRepo = HouseholdRepo(application.applicationContext)

    private val _loginErrorMessage = MutableSharedFlow<String?>()
    val loginErrorMessage: SharedFlow<String?> = _loginErrorMessage.asSharedFlow()

    private val _signupErrorMessage = MutableSharedFlow<String?>()
    val signupErrorMessage: SharedFlow<String?> = _signupErrorMessage.asSharedFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun login(household: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.login(household, password)
            if (success) {
                _isAuthenticated.value = true
                _loginErrorMessage.emit(null) // Clear any previous error
            } else {
                _loginErrorMessage.emit("Invalid credentials")
                _isAuthenticated.value = false
            }
            onComplete(success)
        }
    }

    fun register(household: String, email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (repository.register(household, email, password)) {
                is HouseholdRepo.RegistrationResult.Success -> {
                    _isAuthenticated.value = true
                    _signupErrorMessage.emit(null) // Clear any previous error
                    onComplete(true)
                }
                is HouseholdRepo.RegistrationResult.Duplicate -> {
                    _signupErrorMessage.emit("Household name or email already exists")
                    _isAuthenticated.value = false
                    onComplete(false)
                }
            }
        }
    }

    // New function to handle password mismatch error from UI
    fun setSignupError(message: String) {
        viewModelScope.launch {
            _signupErrorMessage.emit(message)
        }
    }
}