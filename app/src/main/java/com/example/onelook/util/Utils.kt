package com.example.onelook.util

fun Int.toTimeString(): String {
    val str = toString()
    return when (str.length) {
        2 -> str
        else -> "0$str"
    }
}

val String.capital: String
    get() = replaceFirstChar { it.uppercase() }

fun getTimeFromDosagesNumber(dosageIdx: Int, dosagesNumber: Int): String {
    val interval = 24 / dosagesNumber
    val hour = interval * dosageIdx
    val minute = 0
    return "${hour.toTimeString()}:${minute.toTimeString()}"
}