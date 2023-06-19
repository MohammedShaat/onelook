package com.example.onelook.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.DomainActivity
import com.example.onelook.data.domain.Supplement
import com.example.onelook.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _activitiesEvent = MutableSharedFlow<ActivitiesEvent>()
    val activitiesEvent = _activitiesEvent.asSharedFlow()

    private val _activities = MutableSharedFlow<Flow<CustomResult<List<DomainActivity>>>>()
    val activities = _activities.flatMapLatest {
        it
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CustomResult.Loading())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        fetchActivities()
    }

    fun onButtonAddActivityClicked() = viewModelScope.launch {
        _activitiesEvent.emit(ActivitiesEvent.NavigateToAddActivityFragment)
    }

    private fun fetchActivities(forceRefresh: Boolean = false) = viewModelScope.launch {
        _activities.emit(
            repository.getActivities(
                onLoading = {
                    _isLoading.emit(true)
                },
                onForceRefresh = {
                    _isRefreshing.emit(true)
                },
                onForceRefreshFailed = { exception ->
                    _activitiesEvent.emit(ActivitiesEvent.ShowRefreshFailedMessage(exception))
                },
                onFinish = {
                    _isLoading.emit(false)
                    _isRefreshing.emit(false)
                },
                forceRefresh = forceRefresh
            )
        )
    }

    fun onSwipeRefreshSwiped() {
        fetchActivities(true)
    }

    fun onEditActivityClicked(activity: DomainActivity) = viewModelScope.launch {
        _activitiesEvent.emit(ActivitiesEvent.NavigateToAddEditActivityFragmentForEditing(activity))
    }

    sealed class ActivitiesEvent {
        object NavigateToAddActivityFragment : ActivitiesEvent()
        class ShowRefreshFailedMessage(val exception: Exception) : ActivitiesEvent()
        data class NavigateToAddEditActivityFragmentForEditing(val activity: DomainActivity) :
            ActivitiesEvent()
    }
}