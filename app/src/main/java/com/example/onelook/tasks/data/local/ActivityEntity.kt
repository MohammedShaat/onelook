package com.example.onelook.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity("activities")
data class ActivityEntity(
    @PrimaryKey val id: UUID,
    val type: String,
    @ColumnInfo("time_of_day") val timeOfDay: String,
    val duration: String,
    val reminder: String?,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)
