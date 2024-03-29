package com.example.onelook.timer.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcelable
import com.example.onelook.common.presentation.MainActivity
import com.example.onelook.common.util.ACTION_OPEN_TIMER
import com.example.onelook.common.util.ACTION_TIMER_PLAYING
import com.example.onelook.common.util.ACTION_TIMER_VALUE
import com.example.onelook.common.util.ACTIVITIES_TIMER_CHANNEL_ID
import com.example.onelook.common.util.DATE_TIME_FORMAT
import com.example.onelook.common.util.OPEN_TIMER_REQ
import com.example.onelook.common.util.TIMER_ONGOING_NOTIFICATION_ID
import com.example.onelook.common.util.getHours
import com.example.onelook.common.util.getMinutes
import com.example.onelook.common.util.getNotification
import com.example.onelook.common.util.to24Format
import com.example.onelook.tasks.data.mapper.toActivityHistoryEntity
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.tasks.doamin.repository.ActivityHistoryRepository
import com.example.onelook.timer.presentation.TimerViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    @ApplicationContext
    lateinit var context: Context

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var activityHistoryRepository: ActivityHistoryRepository

    private var playPauseReceiver: BroadcastReceiver? = null

    private var limitHour = -1
    private var limitMinute = -1
    private var counterHour = 0
    private var counterMinute = 0
    private var counterSecond = -1
    private val interval = 1000L    //1000L


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
            timer = getFormattedTimer()
            sendOnGoingNotification()
            notifyFragment(TimerViewModel.TimerValueStatus.NewValue)
            Timber.i("service::$timer")
            // Stops when reaching the specified duration
            if (isDurationAchieved()) {
                pauseTimer()
                saveProgressOfActivityHistory()
            } else
                handler.postDelayed(this, interval)
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.i("service::onCreate")
        isRunning = true
        registerPlayPauseReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentActivityHistory =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent?.getParcelableExtra("activity_history", ActivityHistory::class.java)
            else
                intent?.getParcelableExtra("activity_history")
        setupCounters()
        playTimer()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("service::onDestroy")
        pauseTimer()
        currentActivityHistory = null
        isRunning = false
        unRegisterPlayPauseReceiver()
        coroutineScope.cancel()
    }

    private fun registerPlayPauseReceiver() {
        playPauseReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != ACTION_TIMER_PLAYING) return
                when (intent.getParcelableExtra<TimerPlayingStatus>("timer_playing_status")!!) {
                    TimerPlayingStatus.PLAY -> playTimer()
                    TimerPlayingStatus.PAUSE -> pauseTimer()
                    TimerPlayingStatus.STOP -> {
                        pauseTimer()
                        saveProgressOfActivityHistory()
                    }
                }
            }
        }
        registerReceiver(playPauseReceiver, IntentFilter(ACTION_TIMER_PLAYING))
    }

    private fun unRegisterPlayPauseReceiver() {
        playPauseReceiver?.let {
            unregisterReceiver(it)
            playPauseReceiver = null
        }
    }

    private fun playTimer() {
        if (isPlaying) return
        handler.post(runnable)
        isPlaying = true
    }

    private fun pauseTimer() {
        handler.removeCallbacks(runnable)
        isPlaying = false
    }

    private fun setupCounters() {
        currentActivityHistory ?: return
        limitHour = currentActivityHistory!!.duration.getHours()
        limitMinute = currentActivityHistory!!.duration.getMinutes()
        counterHour = currentActivityHistory!!.progress.getHours()
        counterMinute = currentActivityHistory!!.progress.getMinutes()
    }

    private fun getFormattedTimer(): String {
        val second = max(counterSecond, 0)
        return "${counterHour.to24Format()}:${counterMinute.to24Format()}:${second.to24Format()}"
    }

    private fun isDurationAchieved(): Boolean {
        return counterHour == limitHour && counterMinute == limitMinute
    }

    private fun sendOnGoingNotification() {
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_TIMER
            putExtra("activity_history", currentActivityHistory)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            OPEN_TIMER_REQ,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        startForeground(
            TIMER_ONGOING_NOTIFICATION_ID,
            getNotification(context, timer, ACTIVITIES_TIMER_CHANNEL_ID, pendingIntent)
        )
    }

    private fun notifyFragment(status: TimerViewModel.TimerValueStatus) {
        val intent = Intent(ACTION_TIMER_VALUE).apply {
            putExtra("timer_value", timer)
            putExtra("timer_value_status", status)
        }
        context.sendBroadcast(intent)
    }

    private fun saveProgressOfActivityHistory() = coroutineScope.launch {
        isSaving = true
        counterSecond = 0
        timer = getFormattedTimer()
        notifyFragment(TimerViewModel.TimerValueStatus.Save)
        if (currentActivityHistory == null) {
            isSaving = false
            notifyFragment(TimerViewModel.TimerValueStatus.Done)
            stopSelf()
            return@launch
        }
        val newProgress = "${counterHour.to24Format()}:${counterMinute.to24Format()}"
        val timeNowFormatted = SimpleDateFormat(
            DATE_TIME_FORMAT,
            Locale.getDefault()
        ).format(Calendar.getInstance().time)
        val localActivityHistory = currentActivityHistory!!.toActivityHistoryEntity().copy(
            progress = newProgress,
            completed = newProgress == currentActivityHistory!!.duration,
            updatedAt = timeNowFormatted
        )
        activityHistoryRepository.updateActivityHistory(localActivityHistory).collect()
        isSaving = false
        notifyFragment(TimerViewModel.TimerValueStatus.Done)
        stopSelf()
    }

    @Parcelize
    sealed class TimerPlayingStatus : Parcelable {
        object PLAY : TimerPlayingStatus()
        object PAUSE : TimerPlayingStatus()
        object STOP : TimerPlayingStatus()
    }

    companion object {
        var isRunning = false
            private set
        var isPlaying = false
            private set
        var isSaving = false
            private set
        var timer = "00:00:00"
            private set
        var currentActivityHistory: ActivityHistory? = null
            private set
    }
}