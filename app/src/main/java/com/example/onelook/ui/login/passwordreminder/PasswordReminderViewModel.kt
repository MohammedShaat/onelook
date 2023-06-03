package com.example.onelook.ui.login.passwordreminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class PasswordReminderViewModel @Inject constructor(
    private val state: SavedStateHandle,
    val auth: FirebaseAuth
) : ViewModel() {

    val email = state.getLiveData("email", "")

    private val _passwordReminder1Event = MutableSharedFlow<PasswordReminder1Event>()
    val passwordReminder1Event = _passwordReminder1Event.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onButtonConfirmEmailClicked() = viewModelScope.launch {
        val email = email.value!!

        if (!emailIsValid()) {
            _passwordReminder1Event.emit(PasswordReminder1Event.ShowEmptyEmailFieldMessage)
            return@launch
        }

        _isLoading.emit(true)
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            viewModelScope.launch {
                if (task.isSuccessful) {
                    sendingPasswordResetEmailSucceeded()
                } else {
                    _isLoading.emit(false)
                    sendingPasswordResetEmailFailed(task.exception)
                }
            }
        }
    }

    private fun sendingPasswordResetEmailFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                _passwordReminder1Event.emit(
                    PasswordReminder1Event.ShowSendPasswordResetEmailFailedMessage(
                        SendPasswordResetEmailExceptions.INVALID_EMAIL
                    )
                )
            }
            is FirebaseNetworkException -> {
                _passwordReminder1Event.emit(
                    PasswordReminder1Event.ShowSendPasswordResetEmailFailedMessage(
                        SendPasswordResetEmailExceptions.NETWORK_ISSUE
                    )
                )
            }
            else -> {
                _passwordReminder1Event.emit(
                    PasswordReminder1Event.ShowSendPasswordResetEmailFailedMessage(
                        SendPasswordResetEmailExceptions.OTHER_EXCEPTIONS,
                        exception?.localizedMessage
                    )
                )
            }
        }
    }

    private fun sendingPasswordResetEmailSucceeded() = viewModelScope.launch {
        _isLoading.emit(false)
        _passwordReminder1Event.emit(PasswordReminder1Event.NavigateBackToLoginFragment)
    }

    private fun emailIsValid(): Boolean {
        return email.value!!.isNotBlank()
    }

    sealed class PasswordReminder1Event {
        object ShowEmptyEmailFieldMessage : PasswordReminder1Event()

        data class ShowSendPasswordResetEmailFailedMessage(
            val exception: SendPasswordResetEmailExceptions,
            val message: String? = null
        ) : PasswordReminder1Event()

        object NavigateBackToLoginFragment: PasswordReminder1Event()
    }

    enum class SendPasswordResetEmailExceptions {
        INVALID_EMAIL, NETWORK_ISSUE, OTHER_EXCEPTIONS
    }
}