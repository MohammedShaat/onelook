package com.example.onelook.data.domain.models

import com.google.gson.annotations.SerializedName
import kotlin.time.Duration

data class ActivityHistory(
    override val id: String,
    @SerializedName("activity_id") val activityId: String,
    val progress: String,
    val completed: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val type: String,
    val duration: String,
) : TodayTask {

    val formattedProgress: Duration
        get() {
            val formattedString = progress.replace(":", "h ") + "m"
            return Duration.parse(formattedString)
        }
    val formattedType: Type
        get() = Type.valueOf(type.uppercase())
    val formattedDuration: Duration
        get() {
            val formattedString = duration.replace(":", "h ") + "m"
            return Duration.parse(formattedString)
        }

    enum class Type {
        RUNNING, WALKING, FITNESS, YOGA, BREATHING, ROLLERSKATING
    }
}