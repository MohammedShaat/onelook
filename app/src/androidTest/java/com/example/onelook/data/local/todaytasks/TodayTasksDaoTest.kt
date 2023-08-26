package com.example.onelook.data.local.todaytasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.onelook.tasks.data.local.ActivityDao
import com.example.onelook.tasks.data.local.ActivityEntity
import com.example.onelook.tasks.data.local.ActivityHistoryDao
import com.example.onelook.tasks.data.local.ActivityHistoryEntity
import com.example.onelook.tasks.data.local.SupplementEntity
import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.tasks.data.local.SupplementHistoryEntity
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.authentication.data.local.UserEntity
import com.example.onelook.authentication.data.local.UserDao
import com.example.onelook.tasks.data.local.TodayTaskDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    private val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"))

    private val user = UserEntity(
        1, "Android Test", "firebaseUid",
        date, date
    )
    private val supplements = listOf(
        SupplementEntity(
            UUID.randomUUID(), "Supplement 1", "tablet", 3, "everyday",
            null, "morning", "before", "before", false,
            date, date
        ),
        SupplementEntity(
            UUID.randomUUID(), "Supplement 2", "drops", 2, "every 2 days",
            null, "evening", "after", "after", true,
            date, date
        ),
        SupplementEntity(
            UUID.randomUUID(), "Supplement 3", "spoon", 2, "every 5 days",
            null, "afternoon", "with", "before", false,
            date, date
        )
    )
    private val activities = listOf(
        ActivityEntity(
            UUID.randomUUID(), "breathing", "evening", "00:10", "before",
            date, date
        ),
        ActivityEntity(
            UUID.randomUUID(), "waking", "morning", "01:30", "before",
            date, date
        ),
        ActivityEntity(
            UUID.randomUUID(), "yoga", "morning", "00:25", "before",
            date, date
        )
    )
    private val supplementsHistory = listOf(
        SupplementHistoryEntity(
            UUID.randomUUID(), supplements[0].id, 1, false,
            date, date
        ),
        SupplementHistoryEntity(
            UUID.randomUUID(), supplements[0].id, 2, true,
            date, date
        ),
        SupplementHistoryEntity(
            UUID.randomUUID(), supplements[1].id, 2, true,
            "2023-06-09 13:00:50", "2023-06-09 13:00:50"
        ),
    )
    private val activitiesHistory = listOf(
        ActivityHistoryEntity(
            UUID.randomUUID(), activities[0].id, "00:05", false,
            date, date
        ),
        ActivityHistoryEntity(
            UUID.randomUUID(), activities[0].id, "00:010", true,
            date, date
        ),
        ActivityHistoryEntity(
            UUID.randomUUID(), activities[1].id, "01:05", false,
            date, date
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
        val todaySupplementTasksResult = todayTasksDao.getTodaySupplementTasks().first()

        // THEN there is a list of today supplement tasks
        assertThat(todaySupplementTasksResult, hasSize(2))
    }

    @Test
    fun getTodayActivityTasks_userId_returnsListOfTodayActivityTasks() = runBlocking {
        // WHEN call getTodayActivityTasks()
        val todaySupplementTasksResult = todayTasksDao.getTodayActivityTasks().first()

        // THEN there is a list of today activity tasks
        assertThat(todaySupplementTasksResult, hasSize(3))
    }
}