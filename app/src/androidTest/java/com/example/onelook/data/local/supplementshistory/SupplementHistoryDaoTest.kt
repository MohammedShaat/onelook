package com.example.onelook.data.local.supplementshistory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.onelook.tasks.data.local.SupplementEntity
import com.example.onelook.tasks.data.local.SupplementDao
import com.example.onelook.authentication.data.local.UserEntity
import com.example.onelook.authentication.data.local.UserDao
import com.example.onelook.tasks.data.local.SupplementHistoryDao
import com.example.onelook.tasks.data.local.SupplementHistoryEntity
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
class SupplementHistoryDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    @Inject
    @Named("test")
    lateinit var supplementDao: SupplementDao

    @Inject
    @Named("test")
    lateinit var supplementHistoryDao: SupplementHistoryDao
    
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

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setupHiltAndDBUser() = runBlocking {
        hiltRule.inject()
        userDao.insertUser(user)
        supplementDao.insertSupplements(supplements)
    }

    @Test
    fun getSupplementsHistory_userId_returnsListOfSupplementsHistory() = runBlocking {
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)
        // WHEN call getSupplementsHistory()
        val supplementsHistoryResult =
            supplementHistoryDao.getSupplementsHistory(supplements[0].id).first()

        // THEN there is a list of SupplementsHistory
        assertThat(supplementsHistoryResult, hasSize(2))
    }

    @Test
    fun getSupplementHistoryById_id_returnsSupplementHistory() = runBlocking {
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)
        // GIVEN a supplementHistory id
        val id = supplementsHistory[0].id

        // WHEN call getSupplementHistoryById()
        val supplementResult = supplementHistoryDao.getSupplementHistoryById(id).first()

        // THEN the supplementHistory is retrieved
        assertThat(supplementResult, equalTo(supplementsHistory[0]))
    }

    @Test
    fun getAllSupplementsHistory_returnsListOfSupplementsHistory() = runBlocking {
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)
        // WHEN call getSupplementsHistory()
        val supplementsHistoryResult =
            supplementHistoryDao.getAllSupplementsHistory().first()

        // THEN there is a list of SupplementsHistory
        assertThat(supplementsHistoryResult, hasSize(3))
    }

    @Test
    fun insertSupplementHistory_supplementHistory_insertsSupplementHistory() = runBlocking {
        // GIVEN an SupplementHistory
        val supplementHistory = supplementsHistory[0]

        // WHEN call insertSupplementHistory()
        supplementHistoryDao.insertSupplementHistory(supplementHistory)

        // THEN the supplementHistory is inserted
        val supplementHistoryResult =
            supplementHistoryDao.getSupplementHistoryById(supplementHistory.id).first()
        assertThat(supplementHistoryResult, equalTo(supplementHistory))
    }

    @Test
    fun insertSupplementsHistory_listOfSupplementsHistory_insertsSupplementsHistory() =
        runBlocking {
            // WHEN call insertSupplementsHistory()
            supplementHistoryDao.insertSupplementsHistory(supplementsHistory)

            // THEN the SupplementsHistory objects are inserted
            val supplementsHistoryResult =
                supplementHistoryDao.getSupplementsHistory(supplements[1].id).first()
            assertThat(supplementsHistoryResult, hasSize(1))
        }

    @Test
    fun updateSupplementHistory_supplementsHistory_updatesSupplementHistory() = runBlocking {
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)
        // GIVEN an SupplementHistory
        val supplementHistory = supplementsHistory[0].copy(completed = true)

        // WHEN call updateSupplementHistory()
        supplementHistoryDao.updateSupplementHistory(supplementHistory)

        // THEN the supplementHistory is updated
        val supplementsHistoryResult =
            supplementHistoryDao.getSupplementHistoryById(supplementHistory.id).first()
        assertThat(
            supplementsHistoryResult.completed,
            not(equalTo(supplementsHistory[0].completed))
        )
    }

    @Test
    fun deleteSupplementHistory_supplementsHistory_deletesSupplementHistory() = runBlocking {
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)
        // GIVEN an SupplementHistory
        val supplementHistory = supplementsHistory[0]

        // WHEN call deleteSupplementHistory()
        supplementHistoryDao.deleteSupplementHistory(supplementHistory)

        // THEN the supplementHistory is deleted
        val supplementsHistoryResult =
            supplementHistoryDao.getSupplementHistoryById(supplementHistory.id).first()
        assertThat(supplementsHistoryResult, nullValue())
    }

    @Test
    fun deleteAllSupplementsHistory_deletesAllSupplementsHistory() = runBlocking {
        supplementHistoryDao.insertSupplementsHistory(supplementsHistory)

        // WHEN call deleteAllSupplementsHistory()
        supplementHistoryDao.deleteAllSupplementsHistory()

        // THEN all supplementsHistory are deleted
        val supplementsHistoryResult = supplementHistoryDao.getAllSupplementsHistory().first()
        assertThat(supplementsHistoryResult, hasSize(0))
    }
}