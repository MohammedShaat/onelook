package com.example.onelook.ui.supplements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.SharedData
import com.example.onelook.data.domain.Supplement
import com.example.onelook.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SupplementsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _supplementsEvent = MutableSharedFlow<SupplementsEvent>()
    val supplementsEvent = _supplementsEvent.asSharedFlow()

    private val _supplements = MutableSharedFlow<Flow<CustomResult<List<Supplement>>>>()
    val supplements = _supplements.flatMapLatest {
        it
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CustomResult.Loading())

    val isRefreshing = supplements.combine(SharedData.isSyncing) { result, isRunning ->
        result is CustomResult.Loading || isRunning
    }

    init {
        fetchSupplements()
    }

    fun onButtonAddSupplementClicked() = viewModelScope.launch {
        _supplementsEvent.emit(SupplementsEvent.NavigateToAddEditSupplementFragment)
    }

    private fun fetchSupplements(forceRefresh: Boolean = false) = viewModelScope.launch {
        _supplements.emit(
            repository.getSupplements(
                onForceRefreshFailed = { exception ->
                    _supplementsEvent.emit(SupplementsEvent.ShowRefreshFailedMessage(exception))
                },
                forceRefresh = forceRefresh
            )
        )
    }

    fun onSwipeRefreshSwiped() {
        fetchSupplements(true)
    }

    fun onEditSupplementClicked(supplement: Supplement) = viewModelScope.launch {
        _supplementsEvent.emit(
            SupplementsEvent.NavigateToAddEditSupplementFragmentForEditing(
                supplement
            )
        )
    }

    fun onDeleteSupplementClicked(supplement: Supplement) = viewModelScope.launch {
        _supplementsEvent.emit(SupplementsEvent.NavigateToDeleteSupplementDialogFragment(supplement))
    }

    sealed class SupplementsEvent {
        object NavigateToAddEditSupplementFragment : SupplementsEvent()
        class ShowRefreshFailedMessage(val exception: Exception) : SupplementsEvent()
        data class NavigateToAddEditSupplementFragmentForEditing(val supplement: Supplement) :
            SupplementsEvent()

        data class NavigateToDeleteSupplementDialogFragment(val supplement: Supplement) :
            SupplementsEvent()
    }
}