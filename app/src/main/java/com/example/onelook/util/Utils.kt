package com.example.onelook.util

fun Int.toTimeString(): String {
    val str = toString()
    return when (str.length) {
        2 -> str
        else -> "0$str"
    }
}