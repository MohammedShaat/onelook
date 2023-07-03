package com.example.onelook.data.local.notifications

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity("notifications")
data class LocalNotification(
    @PrimaryKey val id: UUID,
    val message: String,
    @ColumnInfo("history_type") val historyType: String,
    @ColumnInfo("history_id") val historyId: UUID,
    @ColumnInfo("created_at") val createdAt: String,
)