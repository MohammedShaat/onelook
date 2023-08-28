package com.example.onelook.tasks.data.mapper

import com.example.onelook.tasks.data.local.SupplementEntity
import com.example.onelook.tasks.data.remote.SupplementDto
import com.example.onelook.tasks.doamin.model.Supplement

fun SupplementDto.toSupplementEntity(): SupplementEntity {
    return SupplementEntity(
        id = id,
        name = name,
        form = form,
        dosage = dosage,
        frequency = frequency,
        duration = duration,
        timeOfDay = timeOfDay,
        takingWithMeals = takingWithMeals,
        reminder = reminder,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SupplementEntity.toSupplementDto(): SupplementDto {
    return SupplementDto(
        id = id,
        name = name,
        form = form,
        dosage = dosage,
        frequency = frequency,
        duration = duration,
        timeOfDay = timeOfDay,
        takingWithMeals = takingWithMeals,
        reminder = reminder,
        completed = completed,
        userId = -1,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SupplementEntity.toSupplement(): Supplement {
    return Supplement(
        id = id,
        name = name,
        form = form,
        dosage = dosage,
        frequency = frequency,
        duration = duration,
        timeOfDay = timeOfDay,
        takingWithMeals = takingWithMeals,
        reminder = reminder,
        completed = completed,
        userId = -1,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Supplement.toSupplementEntity(): SupplementEntity {
    return SupplementEntity(
        id = id,
        name = name,
        form = form,
        dosage = dosage,
        frequency = frequency,
        duration = duration,
        timeOfDay = timeOfDay,
        takingWithMeals = takingWithMeals,
        reminder = reminder,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}