package com.example.onelook.tasks.presentation.deleteactivity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.tasks.doamin.model.DomainActivity
import com.example.onelook.tasks.doamin.repository.ActivityRepository
import com.example.onelook.common.util.Resource
import com.example.onelook.tasks.data.mapper.toActivityEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteActivityViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val activity = savedState.getLiveData<DomainActivity>("activity")

    private val _deleteActivityEvent = MutableSharedFlow<DeleteActivityEvent>()
    val deleteActivityEvent = _deleteActivityEvent.asSharedFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    fun onButtonNegativeClicked() = viewModelScope.launch {
        _deleteActivityEvent.emit(DeleteActivityEvent.DismissDialog)
    }

    fun onButtonPositiveClicked() = viewModelScope.launch {
        _isDeleting.emit(true)

        activityRepository.deleteActivity(activity.value!!.toActivityEntity()).collect { result ->
            if (result is Resource.Success) {
                _deleteActivityEvent.emit(
                    DeleteActivityEvent.NavigateBackAfterActivityDeleted(activity.value!!.type)
                )
                _isDeleting.emit(false)
            }
        }
    }

    sealed class DeleteActivityEvent {
        object DismissDialog : DeleteActivityEvent()
        data class NavigateBackAfterActivityDeleted(val activityName: String) :
            DeleteActivityEvent()
    }
}