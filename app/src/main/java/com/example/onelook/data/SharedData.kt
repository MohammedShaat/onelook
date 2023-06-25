package com.example.onelook.data

import kotlinx.coroutines.flow.MutableStateFlow

object SharedData {
    val isSyncing = MutableStateFlow(false)
}