package com.example.onelook.data.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity("users")
data class DatabaseUser(
    @PrimaryKey val id: Int,
    val name: Int,
    val firebase_uid: String,
    @ColumnInfo("updated_at") val updatedAt: String,
    @ColumnInfo("created_at") val createdAt: String,
)