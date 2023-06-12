package com.example.onelook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.TodayTask
import com.example.onelook.data.network.todaytasks.TodayTaskApi
import com.example.onelook.util.CustomResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth,
    private val todayTaskApi: TodayTaskApi,
    private val repository: Repository
) : ViewModel() {

    val userFirstName: String?
        get() {
            return auth.currentUser!!.displayName?.substringBefore(" ")
        }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _todayTasks = MutableSharedFlow<Flow<CustomResult<out List<TodayTask>?>>>()
    val todayTasks = _todayTasks.flatMapLatest { flowResult ->
        flowResult
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CustomResult.Loading())

    private val _homeEvent = MutableSharedFlow<HomeEvent>()
    val homeEvent = _homeEvent.asSharedFlow()

    init {
        fetchTodayTasks()
    }

    private fun fetchTodayTasks(forceRefresh: Boolean = false) = viewModelScope.launch {
        _todayTasks.emit(
            repository.getTodayTasks(
                whileLoading = {
                    _isLoading.emit(true)
                },
                whileRefreshing = {
                    _isLoading.emit(false)
                    _isRefreshing.emit(true)
                },
                onRefreshSucceeded = {
                    _isLoading.emit(false)
                    _isRefreshing.emit(false)
                },
                onRefreshFailed = { exception ->
                    _isLoading.emit(false)
                    _isRefreshing.emit(false)
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

    sealed class HomeEvent {
        data class ShowRefreshFailedMessage(val exception: Exception) : HomeEvent()
        object NavigateToAddTaskDialog : HomeEvent()
    }
}