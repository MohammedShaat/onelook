package com.example.onelook.authentication.presentation.logout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.authentication.domain.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogOutDialogViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
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
        authenticationRepository.signOut()
        _logOutEvent.emit(LogOutEvent.CancelActivityCoroutines)
        _logOutEvent.emit(LogOutEvent.NavigateToLogInFragmentAfterLoggingOut)
        _isLoading.value = false
    }

    sealed class LogOutEvent {
        object DismissDialog : LogOutEvent()
        object NavigateToLogInFragmentAfterLoggingOut : LogOutEvent()
        object CancelActivityCoroutines : LogOutEvent()
    }

}