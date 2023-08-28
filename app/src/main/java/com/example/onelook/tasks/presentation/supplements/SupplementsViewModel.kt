package com.example.onelook.tasks.presentation.supplements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.timer.data.local.Timer
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.tasks.doamin.repository.SupplementRepository
import com.example.onelook.common.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SupplementsViewModel @Inject constructor(
    private val supplementRepository: SupplementRepository,
) : ViewModel() {

    private val _supplementsEvent = MutableSharedFlow<SupplementsEvent>()
    val supplementsEvent = _supplementsEvent.asSharedFlow()

    private val _supplements = MutableSharedFlow<Flow<Resource<List<Supplement>>>>()
    val supplements = _supplements.flatMapLatest {
        it
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Resource.Loading())

    val isRefreshing = supplements.combine(Timer.isSyncing) { result, isRunning ->
        result is Resource.Loading || isRunning
    }

    init {
        fetchSupplements()
    }

    fun onButtonAddSupplementClicked() = viewModelScope.launch {
        _supplementsEvent.emit(SupplementsEvent.NavigateToAddEditSupplementFragment)
    }

    private fun fetchSupplements(forceRefresh: Boolean = false) = viewModelScope.launch {
        _supplements.emit(
            supplementRepository.getSupplements(
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