package com.example.onelook.timer.data.local

import kotlinx.coroutines.flow.MutableStateFlow

object Timer {
    val isSyncing = MutableStateFlow(false)
}