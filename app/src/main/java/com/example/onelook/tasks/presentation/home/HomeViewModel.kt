package com.example.onelook.tasks.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.timer.data.local.Timer
import com.example.onelook.profile_and_settings.doamin.repository.ProfileRepository
import com.example.onelook.timer.service.TimerService
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.tasks.doamin.model.SupplementHistory
import com.example.onelook.tasks.doamin.model.TodayTask
import com.example.onelook.tasks.doamin.repository.TodayTasksRepository
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.isToday
import com.example.onelook.common.util.parseDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val todayTasksRepository: TodayTasksRepository,
    private val profileRepository: ProfileRepository,
    appStateRepositoryImpl: AppStateRepositoryImpl
) : ViewModel() {

    val userFirstName: String?
        get() {
            return profileRepository.getName()?.substringBefore(" ")
        }

    private val _todayTasks = MutableSharedFlow<Flow<Resource<List<TodayTask>>>>()
    val todayTasks = _todayTasks.flatMapLatest { flowResult ->
        flowResult
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Resource.Loading())

    val isRefreshing = todayTasks.combine(Timer.isSyncing) { result, isRunning ->
        result is Resource.Loading || isRunning
    }

    private val _homeEvent = MutableSharedFlow<HomeEvent>()
    val homeEvent = _homeEvent.asSharedFlow()

    val unreadNotifications = appStateRepositoryImpl.getUnreadNotifications()

    val enqueueImmediateDailyTasksWorker =
        appStateRepositoryImpl.getLastDailyTasksWorkerDate().map { !it.parseDate.isToday }

    init {
        fetchTodayTasks()
    }

    private fun fetchTodayTasks(forceRefresh: Boolean = false) = viewModelScope.launch {
        _todayTasks.emit(
            todayTasksRepository.getTodayTasks(
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