package com.example.onelook.tasks.data.local

import androidx.room.Dao
import androidx.room.Query
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.tasks.doamin.model.SupplementHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayTaskDao {

    @Query(
        "SELECT supplements_history.id AS id, " +
                "supplements_history.supplement_id AS supplementId, " +
                "supplements_history.progress AS progress, " +
                "supplements_history.completed AS completed, " +
                "supplements_history.created_at AS createdAt, " +
                "supplements_history.updated_at AS updatedAt, " +
                "supplements.name AS name, " +
                "supplements.form AS form, " +
                "supplements.dosage AS dosage, " +
                "supplements.time_of_day AS timeOfDay, " +
                "supplements.taking_with_meals AS takingWithMeals " +
                "FROM supplements_history INNER JOIN supplements " +
                "ON supplements_history.supplement_id=supplements.id " +
                "WHERE supplements.completed=0 AND strftime('%Y-%m-%d', supplements_history.created_at)=date('now')"
    )
    fun getTodaySupplementTasks(): Flow<List<SupplementHistory>>

    @Query(
        "SELECT activities_history.id AS id, " +
                "activities_history.activity_id AS activityId, " +
                "activities_history.progress AS progress, " +
                "activities_history.completed AS completed, " +
                "activities_history.created_at AS createdAt, " +
                "activities_history.updated_at AS updatedAt, " +
                "activities.type AS type, " +
                "activities.duration AS duration " +
                "FROM activities_history INNER JOIN activities " +
                "ON activities_history.activity_id=activities.id " +
                "WHERE strftime('%Y-%m-%d', activities_history.created_at)=date('now')"
    )
    fun getTodayActivityTasks(): Flow<List<ActivityHistory>>
}