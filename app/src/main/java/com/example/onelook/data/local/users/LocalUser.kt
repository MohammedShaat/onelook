package com.example.onelook.data.local.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("users")
data class LocalUser(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo("firebase_uid") val firebaseUid: String,
    @ColumnInfo("access _token") val accessToken: String,
    @ColumnInfo("updated_at") val updatedAt: String,
    @ColumnInfo("created_at") val createdAt: String,
)