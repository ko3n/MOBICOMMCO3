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
    private val householdDao = AppDatabase.getInstance(application).householdDao()

    private val _people = MutableStateFlow<List<Person>>(emptyList())
    val people: StateFlow<List<Person>> = _people.asStateFlow()

    private val _addPersonError = MutableStateFlow<String?>(null)
    val addPersonError: StateFlow<String?> = _addPersonError.asStateFlow()


    private val _personToEdit = MutableStateFlow<Person?>(null)
    val personToEdit: StateFlow<Person?> = _personToEdit.asStateFlow()

    /**
     * Finds a person from the main list and sets them as the one to be edited.
     * This is called by the SettingsScreen.
     */
    fun selectPersonToEdit(firebaseId: String) {
        // Find the person in the list we already have in memory
        val person = _people.value.find { it.firebaseId == firebaseId }
        _personToEdit.value = person
    }

    /**
     * Updates a person's profile with a new name.
     */
    fun updateUserProfile(newName: String) {
        // Use the person currently loaded into the 'personToEdit' state.
        _personToEdit.value?.let { userToUpdate ->
            val updatedPerson = userToUpdate.copy(name = newName)

            viewModelScope.launch {
                val success = personRepo.updatePerson(updatedPerson)
                if (success) {
                    // 1. Update the state for the person being edited (for the TaskScreen title)
                    _personToEdit.value = updatedPerson

                    // 2. Also, update the main list of people to refresh the PeopleTab instantly.
                    val currentPeopleList = _people.value.toMutableList()
                    val indexToUpdate = currentPeopleList.indexOfFirst { it.firebaseId == updatedPerson.firebaseId }
                    if (indexToUpdate != -1) {
                        currentPeopleList[indexToUpdate] = updatedPerson
                        _people.value = currentPeopleList
                    }
                }
            }
        }
    }


    fun setHousehold(householdName: String, householdEmail: String) {
        viewModelScope.launch {
            val household = householdDao.findByNameOrEmail(householdName, householdEmail)
            household?.let {
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
            _addPersonError.value = null
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