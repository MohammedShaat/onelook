package com.example.onelook.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.notifications.doamin.model.Notification
import com.example.onelook.notifications.doamin.repository.NotificationRepository
import com.example.onelook.timer.service.TimerService
import com.example.onelook.common.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notificationsEvent = MutableSharedFlow<NotificationsEvent>()
    val notificationsEvent = _notificationsEvent.asSharedFlow()

    val notifications = notificationRepository.getNotifications()

    val isLoading = notifications.map { it is Resource.Loading }

    fun onResume() = viewModelScope.launch {
        notificationRepository.resetNotificationsCounter()
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