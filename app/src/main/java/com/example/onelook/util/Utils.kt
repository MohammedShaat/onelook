package com.example.onelook.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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