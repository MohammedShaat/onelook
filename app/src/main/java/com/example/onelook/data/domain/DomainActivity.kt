package com.example.onelook.data.domain

import java.util.*

data class DomainActivity(
    val id: UUID,
    val type: String,
    val timeOfDay: String,
    val duration: String,
    val reminder: String,
    val userId: Int,
    val createdAt: String,
    val updatedAt: String
) {
    val formattedType: ActivityType
        get() = ActivityType.valueOf(type.uppercase())

    enum class ActivityType {
        RUNNING, WALKING, FITNESS, YOGA, BREATHING, ROLLERSKATING
    }
}
