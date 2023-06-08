package com.example.onelook.data.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    "supplements", foreignKeys = [
        ForeignKey(
            entity = DatabaseUser::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DatabaseSupplement(
    @PrimaryKey val id: UUID,
    val name: String,
    val form: String,
    val dosage: Int,
    val frequency: String,
    val duration: String?,
    @ColumnInfo("time_of_day") val timeOfDay: String,
    @ColumnInfo("taking_with_meals") val takingWithMeals: String,
    val reminder: String,
    val completed: Boolean,
    @ColumnInfo("user_id") val userId: Int,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)