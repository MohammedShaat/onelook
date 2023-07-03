package com.example.onelook.ui.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.services.TimerService
import com.example.onelook.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val _timerEvent = MutableSharedFlow<TimerEvent>()
    val timerEvent = _timerEvent.asSharedFlow()

    val activityHistory =
        savedState.get<ActivityHistory>("activityHistory") ?: TimerService.currentActivityHistory

    private val _timer = MutableStateFlow(
        when {
            TimerService.isRunning -> TimerService.timer
            activityHistory != null ->
                "${activityHistory.progress.getHours().to24Format()}:" +
                        "${activityHistory.progress.getMinutes().to24Format()}:" +
                        "00"
            else -> "00:00:00"
        }
    )
    val timer = _timer.asStateFlow()

    private var timerReceiver: BroadcastReceiver? = null

    private val _isPlaying = MutableStateFlow(TimerService.isPlaying)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isStoppable = MutableStateFlow(TimerService.isRunning && !TimerService.isSaving)
    val isStoppable = _isStoppable.asStateFlow()

    private val _isSaving = MutableStateFlow(TimerService.isSaving)
    val isSaving = _isSaving.asStateFlow()

    init {
        Timber.i("activityHistory: $activityHistory")
        registerTimerReceiver()
    }


    fun onButtonPlayClicked() = viewModelScope.launch {
        if (isDurationAchieved()) {
            Timber.i("isDurationAchieved()")
            _timerEvent.emit(TimerEvent.ShowActivityAlreadyFinishedMessage)
            return@launch
        }

        if (!TimerService.isRunning) {
            Timber.i("!isServiceAlreadyStarted()")
            val timerServiceIntent = Intent(context, TimerService::class.java).apply {
                putExtra("activity_history", activityHistory)
            }
            context.startService(timerServiceIntent)
        } else {
            Timber.i("Intent(PLAY_PAUSE_ACTION)")
            val timerStatusIntent = Intent(ACTION_TIMER_PLAYING).apply {
                val timerStatus =
                    if (_isPlaying.value) TimerService.TimerPlayingStatus.PAUSE else TimerService.TimerPlayingStatus.PLAY
                putExtra("timer_playing_status", timerStatus)
            }
            context.sendBroadcast(timerStatusIntent)
        }
        _isPlaying.value = !_isPlaying.value
        _isStoppable.value = true
    }

    fun onButtonStopClicked() {
        _isSaving.value = true
        _isStoppable.value = false
        _isPlaying.value = false
        val timerStatusIntent = Intent(ACTION_TIMER_PLAYING).apply {
            putExtra("timer_playing_status", TimerService.TimerPlayingStatus.STOP)
        }
        context.sendBroadcast(timerStatusIntent)
    }

    private fun isDurationAchieved(): Boolean {
        return timer.value.substringBeforeLast(":") == activityHistory?.duration
    }

    override fun onCleared() {
        super.onCleared()
        unRegisterTimerReceiver()
    }

    private fun registerTimerReceiver() {
        timerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != ACTION_TIMER_VALUE) return

                val status = intent.getParcelableExtra<TimerValueStatus>("timer_value_status")!!
//                if (activityHistory != null)
                _timer.value = intent.getStringExtra("timer_value") ?: _timer.value
                _isPlaying.value = status is TimerValueStatus.NewValue
                _isStoppable.value = status is TimerValueStatus.NewValue
                _isSaving.value = status is TimerValueStatus.Save
            }
        }
        context.registerReceiver(timerReceiver, IntentFilter(ACTION_TIMER_VALUE))
    }

    private fun unRegisterTimerReceiver() {
        timerReceiver?.let {
            context.unregisterReceiver(timerReceiver)
            timerReceiver = null
        }
    }

    sealed class TimerEvent {
        object ShowActivityAlreadyFinishedMessage : TimerEvent()
    }

    @Parcelize
    sealed class TimerValueStatus : Parcelable {
        object NewValue : TimerValueStatus()
        object Save : TimerValueStatus()
        object Done : TimerValueStatus()
    }
}