package com.example.onelook.tasks.data.remote

import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.tasks.doamin.model.SupplementHistory
import com.google.gson.annotations.SerializedName

data class TodayTaskDto(
    @SerializedName("supplement_history") val supplementHistory: SupplementHistoryDto?,
    val supplement: SupplementDto?,
    @SerializedName("activity_history") val activityHistory: ActivityHistoryDto?,
    val activity: ActivityDto?,
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