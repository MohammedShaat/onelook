package com.example.onelook.tasks.doamin.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class DomainActivity(
    val id: UUID,
    val type: String,
    val timeOfDay: String,
    val duration: String,
    val reminder: String?,
    val userId: Int,
    val createdAt: String,
    val updatedAt: String
) : Parcelable {
    val parsedType: ActivityType
        get() = ActivityType.valueOf(type.uppercase())

    enum class ActivityType {
        RUNNING, WALKING, FITNESS, YOGA, BREATHING, ROLLERSKATING
    }
}
