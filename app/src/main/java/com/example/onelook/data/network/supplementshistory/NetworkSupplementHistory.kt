package com.example.onelook.data.network.supplementshistory

import com.google.gson.annotations.SerializedName
import java.util.*

data class NetworkSupplementHistory(
    val id: UUID,
    @SerializedName("supplement_id") val supplementId: UUID,
    val progress: Int,
    val completed: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)