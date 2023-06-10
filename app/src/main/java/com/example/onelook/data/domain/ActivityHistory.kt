package com.example.onelook.data.domain

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.time.Duration

data class ActivityHistory(
    override val id: UUID,
    val activityId: UUID,
    val progress: String,
    val completed: Boolean,
    override val createdAt: String,
    val updatedAt: String,
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