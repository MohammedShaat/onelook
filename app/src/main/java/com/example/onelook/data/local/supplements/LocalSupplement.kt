package com.example.onelook.data.local.supplements

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity("supplements")
data class LocalSupplement(
    @PrimaryKey val id: UUID,
    val name: String,
    val form: String,
    val dosage: Int,
    val frequency: String,
    val duration: String?,
    @ColumnInfo("time_of_day") val timeOfDay: String?,
    @ColumnInfo("taking_with_meals") val takingWithMeals: String,
    val reminder: String?,
    val completed: Boolean,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)