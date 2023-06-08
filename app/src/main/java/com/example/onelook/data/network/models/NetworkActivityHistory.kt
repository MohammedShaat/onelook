package com.example.onelook.data.network.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class NetworkActivityHistory(
    val id: UUID,
    @SerializedName("activity_id") val activityId: String,
    val progress: String,
    val completed: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)