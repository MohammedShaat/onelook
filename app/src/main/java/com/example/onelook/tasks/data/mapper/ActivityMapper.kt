package com.example.onelook.tasks.data.mapper

import com.example.onelook.tasks.data.local.ActivityEntity
import com.example.onelook.tasks.data.remote.ActivityDto
import com.example.onelook.tasks.doamin.model.DomainActivity

fun DomainActivity.toActivityEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        type = type,
        timeOfDay = timeOfDay,
        duration = duration,
        reminder = reminder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ActivityDto.toActivityEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        type = type,
        timeOfDay = timeOfDay,
        duration = duration,
        reminder = reminder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ActivityEntity.toActivityDto(): ActivityDto {
    return ActivityDto(
        id = id,
        type = type,
        timeOfDay = timeOfDay,
        duration = duration,
        reminder = reminder,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userId = 0,
    )
}

fun ActivityEntity.toDomainActivity(): DomainActivity {
    return DomainActivity(
        id = id,
        type = type,
        timeOfDay = timeOfDay,
        duration = duration,
        reminder = reminder,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userId = 0,
    )
}