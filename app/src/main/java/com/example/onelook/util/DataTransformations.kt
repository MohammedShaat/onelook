package com.example.onelook.util

import com.example.onelook.data.domain.TodayTask
import com.example.onelook.data.network.todaytasks.NetworkTodayTask

fun List<NetworkTodayTask>.toDomainModels(): List<TodayTask> {
    return map { task ->
        if (task.supplementHistory != null)
            task.toSupplementHistoryModel()
        else
            task.toActivityHistoryModel()

    }
}