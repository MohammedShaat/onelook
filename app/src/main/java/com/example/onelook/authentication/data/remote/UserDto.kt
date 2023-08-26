package com.example.onelook.authentication.data.remote

import com.google.gson.annotations.SerializedName


data class UserDto(
    val id: Int,
    val name: String,
    @SerializedName("firebase_uid") val firebaseUid: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("created_at") val createdAt: String,
)
