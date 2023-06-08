package com.example.onelook.data.network.models

import com.google.gson.annotations.SerializedName

data class NetworkUser(
    val name: String,
    val firebase_uid: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("created_at")
    val createdAt: String,
    val id: String,
)