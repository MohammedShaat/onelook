package com.example.onelook.data.network.todaytasks

import androidx.test.filters.SmallTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class TodayTaskApiTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var todayTaskApi: TodayTaskApi

    @Before
    fun setupHilt() {
        hiltRule.inject()
    }

    @Test
    fun getTodayTasks_returnsListOfNetworkSupplementAndActivityHistory() = runBlocking {
        // WHEN call getTodayTasks()
        val response = todayTaskApi.getTodayTasks()

        // THEN response contains a list of SupplementHistory and ActivityHistory
        assertThat(response.size, equalTo(5))
    }
}