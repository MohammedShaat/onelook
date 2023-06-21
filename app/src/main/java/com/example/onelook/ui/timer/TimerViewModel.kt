package com.example.onelook.ui.timer

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.di.appmodules.ApplicationCoroutine
import com.example.onelook.util.toLocalModel
import com.example.onelook.util.toTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class TimerViewModel @Inject constructor(
    savedState: SavedStateHandle,
    @ApplicationCoroutine private val coroutineScope: CoroutineScope,
    private val repository: Repository
) : ViewModel() {


    private val _activityHistory = savedState.getLiveData<ActivityHistory>("activityHistory")
    val activityHistory: LiveData<ActivityHistory>
        get() = _activityHistory

    init {
        Timber.i("activityHistory: ${_activityHistory.value}")
    }

    private val _timerEvent = MutableSharedFlow<TimerEvent>()
    val timerEvent = _timerEvent.asSharedFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val limitHour = _activityHistory.value?.formattedDuration?.inWholeHours?.toInt() ?: -1
    private val limitMinute =
        _activityHistory.value?.formattedDuration?.inWholeMinutes?.toInt() ?: -1
    private var counterHour = _activityHistory.value?.formattedProgress?.inWholeHours?.toInt() ?: 0
    private var counterMinute =
        _activityHistory.value?.formattedProgress?.inWholeMinutes?.toInt() ?: -1
    private var counterSecond = -1
    private val interval = 1000L

    private val _timer = MutableStateFlow(getFormattedTimer())
    val timer = _timer.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            counterSecond += 1
            // Increases minute every 60s
            if (counterSecond == 60) {
                counterMinute += 1
                counterSecond = 0
            }
            // Increases hour every 60m
            if (counterMinute == 60) {
                counterHour += 1
                counterMinute = 0
            }
            _timer.value = getFormattedTimer()
            Timber.i(_timer.value)
            // Stops when reaching the specified duration
            if (isDurationAchieved()) {
                _isPlaying.value = false
                handler.removeCallbacks(this)
            } else
                handler.postDelayed(this, interval)
        }
    }

    private fun isDurationAchieved(): Boolean {
        return counterHour == limitHour && counterMinute == limitMinute
    }

    private fun getFormattedTimer(): String {
        val minute = max(counterMinute, 0)
        val second = max(counterSecond, 0)
        return "${counterHour.toTimeString()}:${minute.toTimeString()}:${second.toTimeString()}"
    }

    fun onButtonPlayClicked() = viewModelScope.launch {
        if (isDurationAchieved()) {
            _timerEvent.emit(TimerEvent.ShowActivityAlreadyFinishedMessage)
            return@launch
        }

        if (_isPlaying.value) {
            _isPlaying.value = false
            handler.removeCallbacks(runnable)
        } else {
            _isPlaying.value = true
            handler.post(runnable)
        }
    }

    override fun onCleared() {
        super.onCleared()

        Timber.i("onCleared called")
        handler.removeCallbacks(runnable)

        if (_activityHistory.value == null) return
        val newProgress = "${counterHour.toTimeString()}:${counterMinute.toTimeString()}"
        val localActivityHistory = _activityHistory.value!!.toLocalModel().copy(
            progress = newProgress,
            completed = newProgress == _activityHistory.value!!.duration
        )
        coroutineScope.launch {
            repository.updateActivityHistory(localActivityHistory).collect()
        }
    }

    sealed class TimerEvent {
        object ShowActivityAlreadyFinishedMessage : TimerEvent()
    }
}