package com.example.onelook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.domain.models.SupplementHistory
import com.example.onelook.data.domain.models.TodayTask
import com.example.onelook.data.network.OneLookApi
import com.example.onelook.util.toDomainModels
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth,
    private val api: OneLookApi
) : ViewModel() {

    val userFirstName: String?
        get() {
            return auth.currentUser!!.displayName?.substringBefore(" ")
        }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _todayTasks = MutableStateFlow<List<TodayTask>>(emptyList())
    val todayTasks = _todayTasks.asStateFlow()

    init {
        fetchTodayTasks()
    }

    private fun fetchTodayTasks(refreshing: Boolean = false) = viewModelScope.launch {
        _isRefreshing.value = refreshing
        _isLoading.value = !refreshing
        _todayTasks.emit(api.getTodayTasks().toDomainModels())
        _isRefreshing.value = false
        _isLoading.value = false
    }

    fun onSwipeRefreshSwiped() {
        fetchTodayTasks(true)
    }
}