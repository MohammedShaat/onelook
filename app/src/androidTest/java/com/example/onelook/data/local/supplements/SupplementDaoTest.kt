package com.example.onelook.data.local.supplements

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
class SupplementDaoTest {

    @Inject
    @Named("test")
    lateinit var userDao: UserDao

    @Inject
    @Named("test")
    lateinit var supplementDao: SupplementDao
    
    private val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"))

    private val user = LocalUser(
        1, "Android Test", "firebaseUid",
        date, date
    )
    private val supplements = listOf(
        LocalSupplement(
            UUID.randomUUID(), "Supplement 1", "tablet", 3, "everyday",
            null, "morning", "before", "before", false,
            date, date
        ),
        LocalSupplement(
            UUID.randomUUID(), "Supplement 2", "drops", 2, "every 2 days",
            null, "evening", "after", "after", true,
            date, date
        ),
        LocalSupplement(
            UUID.randomUUID(), "Supplement 3", "spoon", 2, "every 5 days",
            null, "afternoon", "with", "before", false,
            date, date
        )
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setupHiltAndDBUser() = runBlocking {
        hiltRule.inject()
        userDao.insertUser(user)
    }

    @Test
    fun getSupplements_returnsListOfSupplements() = runBlocking {
        supplementDao.insertSupplements(supplements)
        // WHEN call getSupplements()
        val supplementsResult = supplementDao.getSupplements().first()

        // THEN there is a list of supplements
        assertThat(supplementsResult, hasSize(3))
    }

    @Test
    fun getSupplementById_id_returnsSupplement() = runBlocking {
        supplementDao.insertSupplements(supplements)
        // GIVEN a supplement id
        val id = supplements[0].id

        // WHEN call getSupplementById()
        val supplementResult = supplementDao.getSupplementById(id).first()

        // THEN the supplement is retrieved
        assertThat(supplementResult, equalTo(supplements[0]))
    }

    @Test
    fun insertSupplement_supplement_insertsSupplement() = runBlocking {
        // GIVEN a Supplement
        val supplement = supplements[0]

        // WHEN call insertSupplement()
        supplementDao.insertSupplement(supplement)

        // THEN the supplement is inserted
        val supplementResult = supplementDao.getSupplementById(supplement.id).first()
        assertThat(supplementResult, equalTo(supplement))
    }

    @Test
    fun insertSupplements_listOfSupplements_insertsSupplements() = runBlocking {
        // GIVEN a list of supplements
        // WHEN call insertSupplements()
        supplementDao.insertSupplements(supplements)

        // THEN the supplements are inserted
        val supplementsResult = supplementDao.getSupplements().first()
        assertThat(supplementsResult, hasSize(3))
    }

    @Test
    fun updateSupplement_supplement_updatesSupplement() = runBlocking {
        supplementDao.insertSupplements(supplements)
        // GIVEN a Supplement
        val supplement = supplements[0].copy(name = "Updated Supplement")

        // WHEN call updateSupplement()
        supplementDao.updateSupplement(supplement)

        // THEN the supplement is updated
        val supplementsResult = supplementDao.getSupplementById(supplement.id).first()
        assertThat(supplementsResult.name, not(equalTo(supplements[0].name)))
    }

    @Test
    fun deleteSupplement_supplement_deletesSupplement() = runBlocking {
        supplementDao.insertSupplements(supplements)
        // GIVEN a Supplement
        val supplement = supplements[0]

        // WHEN call deleteSupplement()
        supplementDao.deleteSupplement(supplement)

        // THEN the supplement is deleted
        val supplementsResult = supplementDao.getSupplementById(supplement.id).first()
        assertThat(supplementsResult, nullValue())
    }
}