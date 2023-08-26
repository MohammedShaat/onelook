package com.example.onelook.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    "activities_history")
data class ActivityHistoryEntity(
    @PrimaryKey val id: UUID,
    @ColumnInfo("activity_id") val activityId: UUID,
    val progress: String,
    val completed: Boolean,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)
