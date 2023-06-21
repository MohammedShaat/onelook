package com.example.onelook.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(

) : ViewModel() {

    private val _settingsEvent = MutableSharedFlow<SettingsEvent>()
    val settingsEvent = _settingsEvent.asSharedFlow()

    fun onActivityManagerClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.NavigateToActivityManagerFragment)
    }

    fun onPrivacyPolicyClicked() = viewModelScope.launch {
        _settingsEvent.emit(SettingsEvent.OpenExternalLinkOfPrivacyPolicy)
    }

    sealed class SettingsEvent {
        object NavigateToActivityManagerFragment : SettingsEvent()
        object OpenExternalLinkOfPrivacyPolicy : SettingsEvent()
    }
}