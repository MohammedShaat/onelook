package com.example.onelook.tasks.presentation.deletesupplement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.tasks.doamin.repository.SupplementRepository
import com.example.onelook.common.util.Resource
import com.example.onelook.tasks.data.mapper.toSupplementEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteSupplementViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val supplementRepository: SupplementRepository,
) : ViewModel() {

    private val supplement = savedState.getLiveData<Supplement>("supplement")

    private val _deleteSupplementEvent = MutableSharedFlow<DeleteSupplementEvent>()
    val deleteSupplementEvent = _deleteSupplementEvent.asSharedFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    fun onButtonNegativeClicked() = viewModelScope.launch {
        _deleteSupplementEvent.emit(DeleteSupplementEvent.DismissDialog)
    }

    fun onButtonPositiveClicked() = viewModelScope.launch {
        _isDeleting.emit(true)

        supplementRepository.deleteSupplement(supplement.value!!.toSupplementEntity()).collect { result ->
            if (result is Resource.Success) {
                _deleteSupplementEvent.emit(
                    DeleteSupplementEvent.NavigateBackAfterSupplementDeleted(supplement.value!!.name)
                )
                _isDeleting.emit(false)
            }
        }
    }

    sealed class DeleteSupplementEvent {
        object DismissDialog : DeleteSupplementEvent()
        data class NavigateBackAfterSupplementDeleted(val supplementName: String) :
            DeleteSupplementEvent()
    }
}