package com.example.onelook.tasks.data.mapper

import com.example.onelook.tasks.data.local.SupplementHistoryEntity
import com.example.onelook.tasks.data.remote.SupplementHistoryDto
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.tasks.doamin.model.SupplementHistory

fun SupplementHistoryDto.toSupplementHistoryEntity(): SupplementHistoryEntity {
    return SupplementHistoryEntity(
        id = id,
        supplementId = supplementId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SupplementHistoryEntity.toSupplementHistoryDto(): SupplementHistoryDto {
    return SupplementHistoryDto(
        id = id,
        supplementId = supplementId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SupplementHistory.toSupplementHistoryEntity(): SupplementHistoryEntity {
    return SupplementHistoryEntity(
        id = id,
        supplementId = supplementId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SupplementHistoryEntity.toSupplementHistory(supplement: Supplement): SupplementHistory {
    return SupplementHistory(
        id = id,
        supplementId = supplementId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = supplement.name,
        form = supplement.form,
        dosage = supplement.dosage,
        timeOfDay = supplement.timeOfDay,
        takingWithMeals = supplement.takingWithMeals,
    )
}