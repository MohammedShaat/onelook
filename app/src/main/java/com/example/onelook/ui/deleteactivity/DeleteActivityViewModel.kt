package com.example.onelook.ui.deleteactivity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.DomainActivity
import com.example.onelook.util.CustomResult
import com.example.onelook.util.toLocalModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteActivityViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: Repository
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

        repository.deleteActivity(activity.value!!.toLocalModel()).collect { result ->
            if (result is CustomResult.Success) {
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