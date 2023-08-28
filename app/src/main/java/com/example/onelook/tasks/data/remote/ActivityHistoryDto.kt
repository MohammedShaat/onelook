package com.example.onelook.tasks.data.remote

import com.google.gson.annotations.SerializedName
import java.util.*

data class ActivityHistoryDto(
    val id: UUID,
    @SerializedName("activity_id") val activityId: UUID,
    val progress: String,
    val completed: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)