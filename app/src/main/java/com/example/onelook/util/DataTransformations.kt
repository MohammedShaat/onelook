package com.example.onelook.util

import com.example.onelook.data.domain.*
import com.example.onelook.data.local.activities.LocalActivity
import com.example.onelook.data.local.activitieshistory.LocalActivityHistory
import com.example.onelook.data.local.supplements.LocalSupplement
import com.example.onelook.data.local.supplementshistory.LocalSupplementHistory
import com.example.onelook.data.local.users.LocalUser
import com.example.onelook.data.network.activities.NetworkActivity
import com.example.onelook.data.network.activitieshistory.NetworkActivityHistory
import com.example.onelook.data.network.supplements.NetworkSupplement
import com.example.onelook.data.network.supplementshistory.NetworkSupplementHistory
import com.example.onelook.data.network.todaytasks.NetworkTodayTask
import com.example.onelook.data.network.users.NetworkUserLoginResult

fun List<NetworkTodayTask>.toDomainModels(): List<TodayTask> {
    return map { task ->
        if (task.supplementHistory != null)
            task.toSupplementHistoryModel()
        else
            task.toActivityHistoryModel()

    }
}

fun NetworkActivity.toLocalModel(): LocalActivity {
    return LocalActivity(
        id = id,
        type = type,
        timeOfDay = timeOfDay,
        duration = duration,
        reminder = reminder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NetworkSupplement.toLocalModel(): LocalSupplement {
    return LocalSupplement(
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

fun NetworkSupplementHistory.toLocalModel(): LocalSupplementHistory {
    return LocalSupplementHistory(
        id = id,
        supplementId = supplementId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun NetworkActivityHistory.toLocalModel(): LocalActivityHistory {
    return LocalActivityHistory(
        id = id,
        activityId = activityId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun NetworkUserLoginResult.NetworkUser.toLocalModel(): LocalUser {
    return LocalUser(
        id = id,
        name = name,
        firebaseUid = firebaseUid,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}

fun List<TodayTask>.sortByDate(): List<TodayTask> {
    return sortedByDescending { it.createdAt }
}

fun LocalSupplement.toNetworkModel(): NetworkSupplement {
    return NetworkSupplement(
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

fun LocalSupplementHistory.toNetworkModel(): NetworkSupplementHistory {
    return NetworkSupplementHistory(
        id = id,
        supplementId = supplementId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun LocalActivity.toNetworkModel(): NetworkActivity {
    return NetworkActivity(
        id = id,
        type = type,
        duration = duration,
        timeOfDay = timeOfDay,
        reminder = reminder,
        userId = -1,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun LocalActivityHistory.toNetworkModel(): NetworkActivityHistory {
    return NetworkActivityHistory(
        id = id,
        activityId = activityId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun LocalActivity.toDomainModel(): DomainActivity {
    return DomainActivity(
        id = id,
        type = type,
        timeOfDay = timeOfDay,
        duration = duration,
        reminder = reminder,
        userId = -1,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun LocalSupplement.toDomainModel(): Supplement {
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

fun Supplement.toLocalModel(): LocalSupplement {
    return LocalSupplement(
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

fun DomainActivity.toLocalModel(): LocalActivity {
    return LocalActivity(
        id = id,
        type = type,
        timeOfDay = timeOfDay,
        duration = duration,
        reminder = reminder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SupplementHistory.toLocalModel(): LocalSupplementHistory {
    return LocalSupplementHistory(
        id = id,
        supplementId = supplementId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun ActivityHistory.toLocalModel(): LocalActivityHistory {
    return LocalActivityHistory(
        id = id,
        activityId = activityId,
        progress = progress,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}