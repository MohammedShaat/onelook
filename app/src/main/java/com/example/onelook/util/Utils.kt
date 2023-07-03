package com.example.onelook.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun Int.to24Format(): String {
    val str = toString()
    return when (str.length) {
        2 -> str
        else -> "0$str"
    }
}

fun Long.to24Format(): String {
    val str = toString()
    return when (str.length) {
        2 -> str
        else -> "0$str"
    }
}

val String.capital: String
    get() = replaceFirstChar { it.uppercase() }

fun String.getHours(): Int {
    return substringBefore(":").toInt()
}

fun String.getMinutes(): Int {
    return substringAfter(":").toInt()
}

fun getTimeOfDosage(dosageIdx: Int, dosagesNumber: Int): String {
    val interval = 24 / dosagesNumber
    val hour = interval * dosageIdx
    val minute = 0
    return "${hour.to24Format()}:${minute.to24Format()}"
}

fun getNotificationManager(context: Context): NotificationManager? {
    return ContextCompat.getSystemService(context, NotificationManager::class.java)
}

val String.parse: Date
    get() = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).parse(this) as Date

fun dateStr(): String {
    return SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        .format(Date())
}

fun timeStr(): String {
    return Calendar.getInstance().run {
        "${get(Calendar.HOUR_OF_DAY).to24Format()}:${get(Calendar.MINUTE).to24Format()}"
    }
}

fun String.dayPartTo24Format(): String {
    return when (this) {
        "morning" -> "07:00"
        "afternoon" -> "12:00"
        "evening" -> "18:00"
        "night" -> "21:00"
        else -> "00:00"
    }
}

fun getUpcomingTimeFromDosages(dosagesNumber: Int): String {
    val times = (0 until dosagesNumber).map { getTimeOfDosage(it, dosagesNumber) }
    val timeNow = timeStr()
    return times.firstOrNull { it > timeNow } ?: times.first()
}

fun getExpirationCalendar(creationDate: Date, duration: String?): Calendar? {
    return Calendar.getInstance().run {
        time = creationDate
        val amount = Regex("""\d+""").find(duration ?: "")?.value?.toInt() ?: 0
        add(Calendar.DAY_OF_YEAR, amount)
        if (time != creationDate) this
        else null
    }
}

fun isExpired(creationDate: Date, duration: String?): Boolean {
    val expirationCalendar = getExpirationCalendar(creationDate, duration)
    return expirationCalendar != null && Calendar.getInstance() > expirationCalendar
}

val Date.isToday: Boolean
    get() {
        val todayCalendar = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = this@isToday }
        return todayCalendar.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                todayCalendar.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH) &&
                todayCalendar.get(Calendar.DAY_OF_MONTH) == dateCalendar.get(Calendar.DAY_OF_MONTH)

    }

fun getInitialDelay(): Long {
    val targetCalendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_YEAR, get(Calendar.DAY_OF_YEAR) + 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return abs(targetCalendar.timeInMillis - Calendar.getInstance().timeInMillis)
}