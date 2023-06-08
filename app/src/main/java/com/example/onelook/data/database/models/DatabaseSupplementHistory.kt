package com.example.onelook.data.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity("supplements_history")
data class DatabaseSupplementHistory(
    @PrimaryKey val id: UUID,
    @ColumnInfo("supplement_id") val supplementId: String,
    val progress: Int,
    val completed: Boolean,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)