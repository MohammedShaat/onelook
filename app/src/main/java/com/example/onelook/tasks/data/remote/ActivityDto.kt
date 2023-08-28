package com.example.onelook.tasks.data.remote

import com.google.gson.annotations.SerializedName
import java.util.*

data class ActivityDto(
    val id: UUID,
    val type: String,
    @SerializedName("time_of_day") val timeOfDay: String,
    val duration: String,
    val reminder: String?,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)