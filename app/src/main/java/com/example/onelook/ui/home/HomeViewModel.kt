package com.example.onelook.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.data.domain.TodayTask
import com.example.onelook.services.TimerService
import com.example.onelook.util.CustomResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth,
    private val repository: Repository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val userFirstName: String?
        get() {
            return auth.currentUser!!.displayName?.substringBefore(" ")
        }

    private val _todayTasks = MutableSharedFlow<Flow<CustomResult<List<TodayTask>>>>()
    val todayTasks = _todayTasks.flatMapLatest { flowResult ->
        flowResult
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CustomResult.Loading())

    val isRefreshing = todayTasks.map { it is CustomResult.Loading }

    private val _homeEvent = MutableSharedFlow<HomeEvent>()
    val homeEvent = _homeEvent.asSharedFlow()

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
        object ShowThereIsActivityRunningMessage : HomeViewModel.HomeEvent()

        data class NavigateToSupplementHistoryDetailsFragment(val supplementHistory: SupplementHistory) :
            HomeEvent()

        data class NavigateToTimerFragment(val activityHistory: ActivityHistory) :
            HomeViewModel.HomeEvent()
    }
}