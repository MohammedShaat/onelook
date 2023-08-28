package com.example.onelook.tasks.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.timer.data.local.Timer
import com.example.onelook.tasks.doamin.model.DomainActivity
import com.example.onelook.tasks.doamin.repository.ActivityRepository
import com.example.onelook.common.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val _activitiesEvent = MutableSharedFlow<ActivitiesEvent>()
    val activitiesEvent = _activitiesEvent.asSharedFlow()

    private val _activities = MutableSharedFlow<Flow<Resource<List<DomainActivity>>>>()
    val activities = _activities.flatMapLatest {
        it
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Resource.Loading())

    val isRefreshing = activities.combine(Timer.isSyncing) { result, isRunning ->
        result is Resource.Loading || isRunning
    }

    init {
        fetchActivities()
    }

    fun onButtonAddActivityClicked() = viewModelScope.launch {
        _activitiesEvent.emit(ActivitiesEvent.NavigateToAddActivityFragment)
    }

    private fun fetchActivities(forceRefresh: Boolean = false) = viewModelScope.launch {
        _activities.emit(
            activityRepository.getActivities(
                onForceRefreshFailed = { exception ->
                    _activitiesEvent.emit(ActivitiesEvent.ShowRefreshFailedMessage(exception))
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

    fun onDeleteActivityClicked(activity: DomainActivity) = viewModelScope.launch {
        _activitiesEvent.emit(ActivitiesEvent.NavigateToDeleteActivityDialogFragment(activity))
    }

    sealed class ActivitiesEvent {
        object NavigateToAddActivityFragment : ActivitiesEvent()
        class ShowRefreshFailedMessage(val exception: Exception) : ActivitiesEvent()
        data class NavigateToAddEditActivityFragmentForEditing(val activity: DomainActivity) :
            ActivitiesEvent()

        data class NavigateToDeleteActivityDialogFragment(val activity: DomainActivity) :
            ActivitiesEvent()
    }
}