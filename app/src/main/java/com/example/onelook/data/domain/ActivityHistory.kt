package com.example.onelook.data.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.time.Duration

@Parcelize
data class ActivityHistory(
    override val id: UUID,
    val activityId: UUID,
    val progress: String,
    val completed: Boolean,
    override val createdAt: String,
    val updatedAt: String,
    val type: String,
    val duration: String,
) : TodayTask, Parcelable {

    val parsedProgress: Duration
        get() {
            val formattedString = progress.replace(":", "h ") + "m"
            return Duration.parse(formattedString)
        }
    val parsedType: DomainActivity.ActivityType
        get() = DomainActivity.ActivityType.valueOf(type.uppercase())
    val parsedDuration: Duration
        get() {
            val formattedString = duration.replace(":", "h ") + "m"
            return Duration.parse(formattedString)
        }
}