package com.example.onelook.ui.supplementhistorydetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.ui.addEditsupplement.AddEditSupplementViewModel
import com.example.onelook.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SupplementHistoryDetailsViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repository: Repository
) : ViewModel() {

    private val _supplementHistory = savedState.getLiveData<SupplementHistory>("supplementHistory")
    val supplementHistory: LiveData<SupplementHistory>
        get() = _supplementHistory

    init {
        Timber.i("supplementHistory:: ${_supplementHistory.value}")
    }

    private val _dosagesList = MutableStateFlow(getDosagesList())
    val dosagesList = _dosagesList.asStateFlow()

    private val _supplementHistoryDetailsEvent = MutableSharedFlow<SupplementHistoryDetailsEvent>()
    val supplementHistoryDetailsEvent = _supplementHistoryDetailsEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private fun getDosagesList(): List<Dosage> {
        return List(_supplementHistory.value!!.dosage) { idx ->
            Dosage(
                id = idx,
                isChecked = idx < _supplementHistory.value!!.progress,
                time = _supplementHistory.value!!.timeOfDay
                    ?: getTimeFromDosagesNumber(idx, _supplementHistory.value!!.dosage)
            )
        }

    }

    fun onCheckboxDosageChanged(isChecked: Boolean, dosage: Dosage) = viewModelScope.launch {
        val newDosagesList = _dosagesList.value.map { it ->
            if (it == dosage) it.copy(isChecked = isChecked)
            else it
        }
        _dosagesList.emit(newDosagesList)
    }

    fun onButtonEditSupplementHistoryClicked() = viewModelScope.launch {
        _isLoading.emit(true)
        val newProgress = _dosagesList.value.count { it.isChecked }
        val localSupplementHistory = _supplementHistory.value!!.toLocalModel().copy(
            progress = newProgress,
            completed = newProgress == _supplementHistory.value!!.dosage,
            updatedAt = SimpleDateFormat(
                DATE_TIME_FORMAT,
                Locale.getDefault()
            ).format(Calendar.getInstance().time)
        )

        repository.updateSupplementHistory(localSupplementHistory).collect { result ->
            if (result is CustomResult.Success) {
                _supplementHistoryDetailsEvent.emit(
                    SupplementHistoryDetailsEvent.NavigateBackAfterSupplementHistoryUpdated
                )
                _isLoading.emit(false)
            }
        }
    }

    sealed class SupplementHistoryDetailsEvent {
        object NavigateBackAfterSupplementHistoryUpdated : SupplementHistoryDetailsEvent()
    }
}