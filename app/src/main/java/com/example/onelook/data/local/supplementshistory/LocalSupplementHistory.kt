package com.example.onelook.data.local.supplementshistory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.onelook.data.local.supplements.LocalSupplement
import java.util.*

@Entity(
    "supplements_history")
data class LocalSupplementHistory(
    @PrimaryKey val id: UUID,
    @ColumnInfo("supplement_id") val supplementId: UUID,
    val progress: Int,
    val completed: Boolean,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)