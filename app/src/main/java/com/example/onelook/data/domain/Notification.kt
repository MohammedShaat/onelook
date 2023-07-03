package com.example.onelook.data.domain

import java.util.UUID

data class Notification(
    val id: UUID,
    val message: String,
    val historyType: String,
    val historyId: UUID,
    val createdAt: String,
    val history: TodayTask
)