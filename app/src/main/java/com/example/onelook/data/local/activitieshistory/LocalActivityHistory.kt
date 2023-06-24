package com.example.onelook.data.local.activitieshistory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.onelook.data.local.activities.LocalActivity
import java.util.*

@Entity(
    "activities_history")
data class LocalActivityHistory(
    @PrimaryKey val id: UUID,
    @ColumnInfo("activity_id") val activityId: UUID,
    val progress: String,
    val completed: Boolean,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)
