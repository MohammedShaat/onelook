package com.example.onelook.util

import com.example.onelook.data.domain.models.TodayTask
import com.example.onelook.data.network.responses.NetworkTodayTasksResponse

fun List<NetworkTodayTasksResponse>.toDomainModels(): List<TodayTask> {
    return map { task ->
        if (task.supplementHistory != null)
            task.toSupplementHistoryModel()
        else
            task.toActivityHistoryModel()

    }
}