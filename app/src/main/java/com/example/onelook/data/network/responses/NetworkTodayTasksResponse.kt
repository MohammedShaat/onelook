package com.example.onelook.data.network.responses

import com.example.onelook.data.domain.models.ActivityHistory
import com.example.onelook.data.domain.models.SupplementHistory
import com.example.onelook.data.network.models.NetworkActivity
import com.example.onelook.data.network.models.NetworkActivityHistory
import com.example.onelook.data.network.models.NetworkSupplement
import com.example.onelook.data.network.models.NetworkSupplementHistory
import com.google.gson.annotations.SerializedName

data class NetworkTodayTasksResponse(
    @SerializedName("supplement_history") val supplementHistory: NetworkSupplementHistory?,
    val supplement: NetworkSupplement?,
    @SerializedName("activity_history") val activityHistory: NetworkActivityHistory?,
    val activity: NetworkActivity?,
) {
    fun toSupplementHistoryModel(): SupplementHistory {
        return SupplementHistory(
            id = supplementHistory!!.id,
            supplementId = supplementHistory.supplementId,
            progress = supplementHistory.progress,
            completed = supplementHistory.completed,
            createdAt = supplementHistory.createdAt,
            updatedAt = supplementHistory.updatedAt,
            name = supplement!!.name,
            form = supplement.form,
            dosage = supplement.dosage,
            takingWithMeals = supplement.takingWithMeals,
        )
    }

    fun toActivityHistoryModel(): ActivityHistory {
        return ActivityHistory(
            id = activityHistory!!.id,
            activityId = activityHistory.activityId,
            progress = activityHistory.progress,
            completed = activityHistory.completed,
            createdAt = activityHistory.createdAt,
            updatedAt = activityHistory.updatedAt,
            type = activity!!.type,
            duration = activity.duration
        )
    }
}