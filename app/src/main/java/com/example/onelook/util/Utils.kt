package com.example.onelook.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat

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
fun getTimeFromDosagesNumber(dosageIdx: Int, dosagesNumber: Int): String {
    val interval = 24 / dosagesNumber
    val hour = interval * dosageIdx
    val minute = 0
    return "${hour.to24Format()}:${minute.to24Format()}"
}

fun getNotificationManager(context: Context): NotificationManager? {
    return ContextCompat.getSystemService(context, NotificationManager::class.java)
}