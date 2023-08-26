package com.example.onelook.profile_and_settings.presentation.activitymanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityManagerViewModel @Inject constructor() : ViewModel() {

    private val _activityManagerEvent = MutableSharedFlow<ActivityManagerEvent>()
    val activityManagerEvent = _activityManagerEvent.asSharedFlow()

    fun onActivitiesClicked() = viewModelScope.launch {
        _activityManagerEvent.emit(ActivityManagerEvent.NavigateToActivitiesFragment)
    }

    fun onSupplementsClicked() = viewModelScope.launch {
        _activityManagerEvent.emit(ActivityManagerEvent.NavigateToSupplementsFragment)
    }

    sealed class ActivityManagerEvent {
        object NavigateToActivitiesFragment : ActivityManagerEvent()
        object NavigateToSupplementsFragment : ActivityManagerEvent()
    }
}