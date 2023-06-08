package com.example.onelook.data.network.models

import com.google.gson.annotations.SerializedName

data class NetworkActivity(
    val id: String,
    val type: String,
    @SerializedName("time_of_day") val timeOfDay: String,
    val duration: String,
    val reminder: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)