package com.example.onelook.data.local.notifications

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
import com.example.onelook.notifications.data.local.NotificationDao
import com.example.onelook.notifications.data.local.NotificationEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class NotificationDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    @Inject
    @Named("test")
    lateinit var activityDao: ActivityDao

    @Inject
    @Named("test")
    lateinit var activityHistoryDao: ActivityHistoryDao

    @Inject
    @Named("test")
    lateinit var supplementDao: SupplementDao

    @Inject
    @Named("test")
    lateinit var supplementHistoryDao: SupplementHistoryDao

    @Inject
    @Named("test")
    lateinit var notificationDao: NotificationDao

    private val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"))

    private val user = UserEntity(
        1, "Android Test", "firebaseUid",
        date, date
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
            date, date
        ),
    )
    private val notifications = listOf(
        NotificationEntity(
            id = UUID.randomUUID(), message = "notification 1", historyType = "activity",
            historyId = activitiesHistory[0].id, createdAt = "date"
        ),
        NotificationEntity(
            id = UUID.randomUUID(), message = "notification 2", historyType = "supplement",
            historyId = supplementsHistory[0].id, createdAt = "date"
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
        activityDao.insertActivities(activities)
        activityHistoryDao.insertActivitiesHistory(activitiesHistory)
        supplementDao.insertSupplements(supplements)
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)
    }

    @Test
    fun getNotifications_returnsListOfNotifications() = runBlocking {
        notificationDao.insertNotifications(notifications)
        // WHEN call getNotifications()
        val notificationsResult = notificationDao.getNotifications().first()

        // THEN there is a list of notifications
        assertThat(notificationsResult, Matchers.hasSize(2))
    }

    @Test
    fun getNotificationById_id_returnsNotification() = runBlocking {
        notificationDao.insertNotifications(notifications)
        // GIVEN a notification id
        val id = notifications[0].id

        // WHEN call getNotificationById()
        val supplementResult = notificationDao.getNotificationById(id).first()

        // THEN the notification is retrieved
        assertThat(supplementResult, Matchers.equalTo(notifications[0]))
    }

    @Test
    fun insertNotification_notification_insertsNotification() = runBlocking {
        // GIVEN a Notification
        val notification = notifications[0]

        // WHEN call insertNotification()
        notificationDao.insertNotification(notification)

        // THEN the notification is inserted
        val notificationResult = notificationDao.getNotificationById(notification.id).first()
        assertThat(notificationResult, Matchers.equalTo(notification))
    }

    @Test
    fun insertNotifications_listOfNotifications_insertsNotifications() = runBlocking {
        // GIVEN a list of notifications
        // WHEN call insertNotifications()
        notificationDao.insertNotifications(notifications)

        // THEN the notifications are inserted
        val notificationsResult = notificationDao.getNotifications().first()
        assertThat(notificationsResult, Matchers.hasSize(2))
    }

    @Test
    fun updateNotification_notification_updatesNotification() = runBlocking {
        notificationDao.insertNotifications(notifications)
        // GIVEN a Notification
        val notification = notifications[0].copy(historyId = activitiesHistory[1].id)

        // WHEN call updateNotification()
        notificationDao.updateNotification(notification)

        // THEN the notification is updated
        val notificationsResult = notificationDao.getNotificationById(notification.id).first()
        assertThat(
            notificationsResult.historyId,
            Matchers.not(Matchers.equalTo(notifications[0].historyId))
        )
    }

    @Test
    fun deleteNotification_notification_deletesNotification() = runBlocking {
        notificationDao.insertNotifications(notifications)
        // GIVEN a Notification
        val notification = notifications[0]

        // WHEN call deleteNotification()
        notificationDao.deleteNotification(notification)

        // THEN the notification is deleted
        val notificationsResult = notificationDao.getNotificationById(notification.id).first()
        assertThat(notificationsResult, Matchers.nullValue())
    }

    @Test
    fun deleteAllNotifications_deletesAllNotifications() = runBlocking {
        notificationDao.insertNotifications(notifications)

        // WHEN call deleteAllNotifications()
        notificationDao.deleteAllNotifications()

        // THEN all notifications are deleted
        val notificationsResult = notificationDao.getNotifications().first()
        assertThat(notificationsResult, hasSize(0))
    }
}