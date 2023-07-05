package com.example.onelook.ui.notificationsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    val areAllNotificationsEnabled = userPreferencesManager.getAllNotificationsState()
    val areActivitiesNotificationsEnabled = userPreferencesManager.getActivitiesNotificationsState()
    val areSupplementsNotificationsEnabled =
        userPreferencesManager.getSupplementsNotificationsState()

    private val _notificationsSettingsEvent = MutableSharedFlow<NotificationsSettingsEvent>()
    val notificationsSettingsEvent = _notificationsSettingsEvent.asSharedFlow()

    fun onSwitchAllNotificationsClicked(isChecked: Boolean) = viewModelScope.launch {
        userPreferencesManager.changeAllNotificationsState(isChecked)
    }

    fun onSwitchActivitiesNotificationsClicked(isChecked: Boolean) = viewModelScope.launch {
        userPreferencesManager.changeActivitiesNotificationsState(isChecked)
    }

    fun onSwitchSupplementsNotificationsClicked(isChecked: Boolean) = viewModelScope.launch {
        userPreferencesManager.changeSupplementsNotificationsState(isChecked)
    }

    sealed class NotificationsSettingsEvent {

    }
}