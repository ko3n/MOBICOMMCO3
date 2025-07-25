package com.mobdeve.s16.group6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s16.group6.data.AppDatabase
import com.mobdeve.s16.group6.data.Person
import com.mobdeve.s16.group6.data.PersonRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PeopleViewModel(application: Application) : AndroidViewModel(application) {

    private val personRepo = PersonRepo(application)
    private val householdDao = AppDatabase.getInstance(application).householdDao() // Get HouseholdDao directly

    private val _people = MutableStateFlow<List<Person>>(emptyList())
    val people: StateFlow<List<Person>> = _people.asStateFlow()

    private val _addPersonError = MutableStateFlow<String?>(null)
    val addPersonError: StateFlow<String?> = _addPersonError.asStateFlow()

    private var currentHouseholdId: Int? = null

    // This function should be called once the user is authenticated and household info is known
    fun setHousehold(householdName: String, householdEmail: String) {
        viewModelScope.launch {
            val household = householdDao.findByNameOrEmail(householdName, householdEmail)
            household?.let {
                currentHouseholdId = it.id
                personRepo.getPeopleForCurrentHousehold(it.id).collectLatest { peopleList ->
                    _people.value = peopleList
                }
            } ?: run {
                _addPersonError.value = "Household not found."
            }
        }
    }

    fun addPerson(personName: String, householdName: String, householdEmail: String) {
        viewModelScope.launch {
            _addPersonError.value = null // Clear previous errors
            val success = personRepo.addPerson(personName, householdName, householdEmail)
            if (!success) {
                _addPersonError.value = "A person with this name already exists in your household."
            }
        }
    }

    fun clearAddPersonError() {
        _addPersonError.value = null
    }
}