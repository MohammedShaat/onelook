package com.example.onelook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.data.SharedData
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.data.domain.TodayTask
import com.example.onelook.di.ApplicationCoroutine
import com.example.onelook.services.TimerService
import com.example.onelook.util.CustomResult
import com.example.onelook.util.isToday
import com.example.onelook.util.parseDate
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth,
    private val repository: Repository,
    private val appStateManager: AppStateManager,
    @ApplicationCoroutine private val applicationCoroutine: CoroutineScope
) : ViewModel() {

    val userFirstName: String?
        get() {
            return auth.currentUser!!.displayName?.substringBefore(" ")
        }

    private val _todayTasks = MutableSharedFlow<Flow<CustomResult<List<TodayTask>>>>()
    val todayTasks = _todayTasks.flatMapLatest { flowResult ->
        flowResult
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CustomResult.Loading())

    val isRefreshing = todayTasks.combine(SharedData.isSyncing) { result, isRunning ->
        result is CustomResult.Loading || isRunning
    }

    private val _homeEvent = MutableSharedFlow<HomeEvent>()
    val homeEvent = _homeEvent.asSharedFlow()

    val unreadNotifications = appStateManager.getUnreadNotifications()

    val enqueueImmediateDailyTasksWorker =
        appStateManager.getLastDailyTasksWorkerDate().map { !it.parseDate.isToday }

    init {
        fetchTodayTasks()
    }

    private fun fetchTodayTasks(forceRefresh: Boolean = false) = viewModelScope.launch {
        _todayTasks.emit(
            repository.getTodayTasks(
                onForceRefreshFailed = { exception ->
                    _homeEvent.emit(HomeEvent.ShowRefreshFailedMessage(exception))
                },
                forceRefresh = forceRefresh
            )
        )
    }

    fun onSwipeRefreshSwiped() {
        fetchTodayTasks(true)
    }

    fun onAddEventClicked() = viewModelScope.launch {
        _homeEvent.emit(HomeEvent.NavigateToAddTaskDialog)
    }

    fun onSupplementHistoryClicked(supplementHistory: SupplementHistory) = viewModelScope.launch {
        _homeEvent.emit(HomeEvent.NavigateToSupplementHistoryDetailsFragment(supplementHistory))
    }

    fun onActivityHistoryClicked(activityHistory: ActivityHistory) = viewModelScope.launch {
        if (TimerService.isRunning && TimerService.currentActivityHistory?.id != activityHistory.id)
            _homeEvent.emit(HomeEvent.ShowThereIsActivityRunningMessage)
        else
            _homeEvent.emit(HomeEvent.NavigateToTimerFragment(activityHistory))
    }

    sealed class HomeEvent {
        data class ShowRefreshFailedMessage(val exception: Exception) : HomeEvent()
        object NavigateToAddTaskDialog : HomeEvent()
        object ShowThereIsActivityRunningMessage : HomeEvent()
        data class NavigateToSupplementHistoryDetailsFragment(val supplementHistory: SupplementHistory) :
            HomeEvent()

        data class NavigateToTimerFragment(val activityHistory: ActivityHistory) :
            HomeEvent()
    }
}