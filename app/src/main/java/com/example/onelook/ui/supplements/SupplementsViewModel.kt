package com.example.onelook.ui.supplements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.Supplement
import com.example.onelook.util.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        fetchSupplements()
    }

    fun onButtonAddSupplementClicked() = viewModelScope.launch {
        _supplementsEvent.emit(SupplementsEvent.NavigateToAddSupplementFragment)
    }

    private fun fetchSupplements(forceRefresh: Boolean = false) = viewModelScope.launch {
        _supplements.emit(
            repository.getSupplements(
                onLoading = {
                    _isLoading.emit(true)
                },
                onForceRefresh = {
                    _isRefreshing.emit(true)
                },
                onForceRefreshFailed = { exception ->
                    _supplementsEvent.emit(SupplementsEvent.ShowRefreshFailedMessage(exception))
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
        fetchSupplements(true)
    }

    sealed class SupplementsEvent {
        object NavigateToAddSupplementFragment : SupplementsEvent()
        class ShowRefreshFailedMessage(val exception: Exception) : SupplementsEvent()
    }
}