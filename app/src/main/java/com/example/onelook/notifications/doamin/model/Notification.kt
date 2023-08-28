package com.example.onelook.notifications.doamin.model

import com.example.onelook.tasks.doamin.model.TodayTask
import java.util.UUID

data class Notification(
    val id: UUID,
    val message: String,
    val historyType: String,
    val historyId: UUID,
    val createdAt: String,
    val history: TodayTask
)