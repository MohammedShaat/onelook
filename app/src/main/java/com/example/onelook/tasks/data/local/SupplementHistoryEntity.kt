package com.example.onelook.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    "supplements_history")
data class SupplementHistoryEntity(
    @PrimaryKey val id: UUID,
    @ColumnInfo("supplement_id") val supplementId: UUID,
    val progress: Int,
    val completed: Boolean,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)