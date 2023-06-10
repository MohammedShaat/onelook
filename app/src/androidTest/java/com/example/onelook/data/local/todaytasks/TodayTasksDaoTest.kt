package com.example.onelook.data.local.todaytasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.onelook.data.local.activities.ActivityDao
import com.example.onelook.data.local.activities.LocalActivity
import com.example.onelook.data.local.activitieshistory.ActivityHistoryDao
import com.example.onelook.data.local.activitieshistory.LocalActivityHistory
import com.example.onelook.data.local.supplements.LocalSupplement
import com.example.onelook.data.local.supplements.SupplementDao
import com.example.onelook.data.local.supplementshistory.LocalSupplementHistory
import com.example.onelook.data.local.supplementshistory.SupplementHistoryDao
import com.example.onelook.data.local.users.LocalUser
import com.example.onelook.data.local.users.UserDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class TodayTasksDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    @Inject
    @Named("test")
    lateinit var supplementDao: SupplementDao

    @Inject
    @Named("test")
    lateinit var activityDao: ActivityDao

    @Inject
    @Named("test")
    lateinit var supplementHistoryDao: SupplementHistoryDao

    @Inject
    @Named("test")
    lateinit var activityHistoryDao: ActivityHistoryDao

    @Inject
    @Named("test")
    lateinit var todayTasksDao: TodayTaskDao

    private val user = LocalUser(
        1, "Android Test",
        "firebaseUid", "accessToken",
        "2023-06-10 11:45:30", "2023-06-10 11:45:30"
    )
    private val supplements = listOf(
        LocalSupplement(
            UUID.randomUUID(), "Supplement 1", "tablet", 3, "everyday",
            null, "morning", "before", "before", false,
            user.id, "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalSupplement(
            UUID.randomUUID(), "Supplement 2", "drops", 2, "every 2 days",
            null, "evening", "after", "after", true,
            user.id, "2023-06-10 13:01:03", "2023-06-10 13:01:03"
        ),
        LocalSupplement(
            UUID.randomUUID(), "Supplement 3", "spoon", 2, "every 5 days",
            null, "afternoon", "with", "before", false,
            user.id, "2023-06-10 13:01:07", "2023-06-10 13:01:07"
        )
    )
    private val activities = listOf(
        LocalActivity(
            UUID.randomUUID(), "breathing", "evening", "00:10", "before",
            user.id, "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivity(
            UUID.randomUUID(), "waking", "morning", "01:30", "before",
            user.id, "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivity(
            UUID.randomUUID(), "yoga", "morning", "00:25", "before",
            user.id, "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        )
    )
    private val supplementsHistory = listOf(
        LocalSupplementHistory(
            UUID.randomUUID(), supplements[0].id, 1, false,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalSupplementHistory(
            UUID.randomUUID(), supplements[0].id, 2, true,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalSupplementHistory(
            UUID.randomUUID(), supplements[1].id, 2, true,
            "2023-06-09 13:00:50", "2023-06-09 13:00:50"
        ),
    )
    private val activitiesHistory = listOf(
        LocalActivityHistory(
            UUID.randomUUID(), activities[0].id, "00:05", false,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivityHistory(
            UUID.randomUUID(), activities[0].id, "00:010", true,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
        LocalActivityHistory(
            UUID.randomUUID(), activities[1].id, "01:05", false,
            "2023-06-10 13:00:50", "2023-06-10 13:00:50"
        ),
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setupHiltAndDBUser() = runBlocking {
        hiltRule.inject()
        userDao.insertUser(user)
        supplementDao.insertSupplements(supplements)
        activityDao.insertActivities(activities)
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)
        activityHistoryDao.insertActivitiesHistory(activitiesHistory)
    }

    @Test
    fun getTodaySupplementTasks_userId_returnsListOfTodaySupplementTasks() = runBlocking {
        // WHEN call getTodaySupplementTasks()
        val todaySupplementTasksResult = todayTasksDao.getTodaySupplementTasks(user.id).first()

        // THEN there is a list of today supplement tasks
        assertThat(todaySupplementTasksResult, hasSize(2))
    }
    @Test
    fun getTodayActivityTasks_userId_returnsListOfTodayActivityTasks() = runBlocking {
        // WHEN call getTodayActivityTasks()
        val todaySupplementTasksResult = todayTasksDao.getTodayActivityTasks(user.id).first()

        // THEN there is a list of today activity tasks
        assertThat(todaySupplementTasksResult, hasSize(3))
    }
}