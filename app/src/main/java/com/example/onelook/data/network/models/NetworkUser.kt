package com.example.onelook.data.network.models

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class NetworkUser(
    val id: Int,
    val name: String,
    val firebase_uid: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("created_at") val createdAt: String,
)