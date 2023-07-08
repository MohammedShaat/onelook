package com.example.onelook.ui.notifications

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.Notification
import com.example.onelook.services.TimerService
import com.example.onelook.ui.home.HomeViewModel
import com.example.onelook.ui.mainactivity.MainActivity
import com.example.onelook.util.ACTION_OPEN_ACTIVITY_NOTIFICATION
import com.example.onelook.util.ACTION_OPEN_SUPPLEMENT_NOTIFICATION
import com.example.onelook.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: Repository,
    private val appStateManager: AppStateManager
) : ViewModel() {

    private val _notificationsEvent = MutableSharedFlow<NotificationsEvent>()
    val notificationsEvent = _notificationsEvent.asSharedFlow()

    val notifications = repository.getNotifications()

    val isLoading = notifications.map { it is CustomResult.Loading }

    fun onResume() = viewModelScope.launch {
        appStateManager.clearUnreadNotifications()
    }

    fun onNotificationClicked(notification: Notification) = viewModelScope.launch {
        if (notification.history is ActivityHistory && TimerService.isRunning &&
            TimerService.currentActivityHistory?.id != notification.history.id
        )
            _notificationsEvent.emit(NotificationsEvent.ShowThereIsActivityRunningMessage)
        else
            _notificationsEvent.emit(NotificationsEvent.OpenTask(notification))

    }


    sealed class NotificationsEvent {
        object ShowThereIsActivityRunningMessage : NotificationsEvent()

        data class OpenTask(val notification: Notification) : NotificationsEvent()
    }
}