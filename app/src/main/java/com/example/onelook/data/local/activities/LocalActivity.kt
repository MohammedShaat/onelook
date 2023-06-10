package com.example.onelook.data.local.activities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.onelook.data.local.users.LocalUser
import java.util.*

@Entity(
    "activities", foreignKeys = [
        ForeignKey(
            entity = LocalUser::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalActivity(
    @PrimaryKey val id: UUID,
    val type: String,
    @ColumnInfo("time_of_day") val timeOfDay: String,
    val duration: String,
    val reminder: String,
    @ColumnInfo("user_id") val userId: Int,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String
)