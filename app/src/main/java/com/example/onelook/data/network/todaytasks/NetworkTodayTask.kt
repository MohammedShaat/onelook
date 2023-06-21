package com.example.onelook.data.network.todaytasks

import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.data.network.activities.NetworkActivity
import com.example.onelook.data.network.activitieshistory.NetworkActivityHistory
import com.example.onelook.data.network.supplementshistory.NetworkSupplementHistory
import com.example.onelook.data.network.supplements.NetworkSupplement
import com.google.gson.annotations.SerializedName

data class NetworkTodayTask(
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
            timeOfDay = supplement.timeOfDay,
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