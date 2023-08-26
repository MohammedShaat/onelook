package com.example.onelook.tasks.data.mapper

import com.example.onelook.tasks.data.local.ActivityHistoryEntity
import com.example.onelook.tasks.data.remote.ActivityHistoryDto
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.tasks.doamin.model.DomainActivity

fun ActivityHistoryDto.toActivityHistoryEntity(): ActivityHistoryEntity {
    return ActivityHistoryEntity(
        id = id,
        activityId = activityId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun ActivityHistoryEntity.toActivityHistoryDto(): ActivityHistoryDto {
    return ActivityHistoryDto(
        id = id,
        activityId = activityId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun ActivityHistory.toActivityHistoryEntity(): ActivityHistoryEntity {
    return ActivityHistoryEntity(
        id = id,
        activityId = activityId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun ActivityHistoryEntity.toActivityHistory(activity: DomainActivity): ActivityHistory {
    return ActivityHistory(
        id = id,
        activityId = activityId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
        type = activity.type,
        duration = activity.duration,
    )
}