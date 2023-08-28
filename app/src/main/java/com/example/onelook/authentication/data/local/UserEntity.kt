package com.example.onelook.authentication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo("firebase_uid") val firebaseUid: String,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String,
)