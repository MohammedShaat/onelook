package com.example.onelook.data.network.models

import com.google.gson.annotations.SerializedName

data class NetworkSupplementHistory(
    val id: String,
    @SerializedName("supplement_id") val supplementId: String,
    val progress: Int,
    val completed: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
