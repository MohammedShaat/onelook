package com.example.onelook.data.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity("activities_history")
data class DatabaseActivityHistory(
    @PrimaryKey val id: UUID,
    @ColumnInfo("activity_id") val activityId: String,
    val progress: String,
    val completed: Boolean,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)
