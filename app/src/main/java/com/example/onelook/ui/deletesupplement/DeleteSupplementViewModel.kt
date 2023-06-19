package com.example.onelook.ui.deletesupplement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.Supplement
import com.example.onelook.ui.addEditsupplement.AddEditSupplementViewModel
import com.example.onelook.util.CustomResult
import com.example.onelook.util.capital
import com.example.onelook.util.toLocalModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteSupplementViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: Repository
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

        repository.deleteSupplement(supplement.value!!.toLocalModel()).collect { result ->
            if (result is CustomResult.Success) {
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