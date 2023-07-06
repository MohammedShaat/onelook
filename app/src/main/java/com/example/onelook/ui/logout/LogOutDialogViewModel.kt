package com.example.onelook.ui.logout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.AppState
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.di.ApplicationCoroutine
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogOutDialogViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: Repository,
    private val appStateManager: AppStateManager,
    @ApplicationCoroutine private val applicationCoroutine: CoroutineScope
) : ViewModel() {

    private val _logOutEvent = MutableSharedFlow<LogOutEvent>()
    val logOutEvent = _logOutEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onButtonNegativeClicked() = viewModelScope.launch {
        _logOutEvent.emit(LogOutEvent.DismissDialog)
    }

    fun onButtonPositiveClicked() = viewModelScope.launch {
        _isLoading.value = true
        applicationCoroutine.coroutineContext.cancelChildren(CancellationException("Logging Out"))
        _logOutEvent.emit(LogOutEvent.CancelActivityCoroutines)
        auth.signOut()
        repository.clearDb().collect()
        appStateManager.clear()
        appStateManager.updateAppState(AppState.LOGGED_OUT)
        _logOutEvent.emit(LogOutEvent.NavigateToLogInFragmentAfterLoggingOut)
        _isLoading.value = false
    }

    sealed class LogOutEvent {
        object DismissDialog : LogOutEvent()
        object NavigateToLogInFragmentAfterLoggingOut : LogOutEvent()
        object CancelActivityCoroutines : LogOutEvent()
    }

}