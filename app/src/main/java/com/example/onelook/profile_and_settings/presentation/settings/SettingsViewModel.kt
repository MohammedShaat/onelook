package com.example.onelook.profile_and_settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _settingsEvent = MutableSharedFlow<SettingsEvent>()
    val settingsEvent = _settingsEvent.asSharedFlow()

    fun onActivityManagerClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.NavigateToActivityManagerFragment)
    }

    fun onPersonalDataClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.NavigateToPersonalDataFragment)
    }

    fun onNotificationsClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.NavigateToNotificationsSettingsFragment)
    }

    fun onContactUsClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.NavigateToContactUsFragment)
    }

    fun onPrivacyPolicyClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.OpenExternalLinkOfPrivacyPolicy)
    }

    fun onLogOutClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.NavigateToLogOutDialogFragment)
    }

    sealed class SettingsEvent {
        object NavigateToActivityManagerFragment : SettingsEvent()
        object NavigateToPersonalDataFragment : SettingsEvent()
        object NavigateToContactUsFragment : SettingsEvent()
        object OpenExternalLinkOfPrivacyPolicy : SettingsEvent()
        object NavigateToLogOutDialogFragment : SettingsEvent()
        object NavigateToNotificationsSettingsFragment : SettingsEvent()
    }
}