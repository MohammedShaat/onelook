package com.example.onelook.notifications.data.mapper

import com.example.onelook.notifications.data.local.NotificationEntity
import com.example.onelook.notifications.doamin.model.Notification
import com.example.onelook.tasks.doamin.model.TodayTask

fun NotificationEntity.toNotification(history: TodayTask): Notification {
    return Notification(
        id = id,
        message = message,
        historyType = historyType,
        historyId = historyId,
        createdAt = createdAt,
        history = history,
    )
}