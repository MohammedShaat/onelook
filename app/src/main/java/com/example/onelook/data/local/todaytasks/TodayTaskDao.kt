package com.example.onelook.data.local.todaytasks

import androidx.room.Dao
import androidx.room.Query
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.SupplementHistory
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
                "supplements.taking_with_meals AS takingWithMeals " +
                "FROM supplements_history INNER JOIN supplements " +
                "ON supplements_history.supplement_id=supplements.id " +
                "WHERE supplements.user_id=:userId AND " +
                "strftime('%Y-%m-%d', supplements_history.created_at)=date('now') " +
                "ORDER BY supplements_history.created_at"
    )
    fun getTodaySupplementTasks(userId: Int): Flow<List<SupplementHistory>>

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
                "WHERE activities.user_id=:userId AND " +
                "strftime('%Y-%m-%d', activities_history.created_at)=date('now') " +
                "ORDER BY activities_history.created_at"
    )
    fun getTodayActivityTasks(userId: Int): Flow<List<ActivityHistory>>
}