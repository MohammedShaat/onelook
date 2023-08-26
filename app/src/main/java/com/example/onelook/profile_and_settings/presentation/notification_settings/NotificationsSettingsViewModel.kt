package com.example.onelook.profile_and_settings.presentation.notification_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.common.data.repository.UserPreferencesRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    private val userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
) : ViewModel() {

    val areAllNotificationsEnabled = userPreferencesRepositoryImpl.getAllNotificationsState()
    val areActivitiesNotificationsEnabled = userPreferencesRepositoryImpl.getActivitiesNotificationsState()
    val areSupplementsNotificationsEnabled =
        userPreferencesRepositoryImpl.getSupplementsNotificationsState()

    private val _notificationsSettingsEvent = MutableSharedFlow<NotificationsSettingsEvent>()
    val notificationsSettingsEvent = _notificationsSettingsEvent.asSharedFlow()

    fun onSwitchAllNotificationsClicked(isChecked: Boolean) = viewModelScope.launch {
        userPreferencesRepositoryImpl.changeAllNotificationsState(isChecked)
    }

    fun onSwitchActivitiesNotificationsClicked(isChecked: Boolean) = viewModelScope.launch {
        userPreferencesRepositoryImpl.changeActivitiesNotificationsState(isChecked)
    }

    fun onSwitchSupplementsNotificationsClicked(isChecked: Boolean) = viewModelScope.launch {
        userPreferencesRepositoryImpl.changeSupplementsNotificationsState(isChecked)
    }

    sealed class NotificationsSettingsEvent {

    }
}