package com.example.onelook.ui.personaldata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class PersonalDataViewModel @Inject constructor(
    val auth: FirebaseAuth
) : ViewModel() {

    val name: String
        get() = auth.currentUser!!.displayName!!
    val email: String?
        get() = auth.currentUser!!.email

    private val _personalDataEvent = MutableSharedFlow<PersonalDataEvent>()
    val personalDataEvent = _personalDataEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onEditNameClicked() = viewModelScope.launch {
        _personalDataEvent.emit(PersonalDataEvent.NavigateToChangeNameFragment)
    }

    fun onChangePasswordClicked() = viewModelScope.launch {
        email ?: return@launch
        _isLoading.value = true
        auth.sendPasswordResetEmail(email!!).addOnCompleteListener { task ->
            viewModelScope.launch {
                if (task.isSuccessful)
                    _personalDataEvent.emit(PersonalDataEvent.ShowPasswordRestEmailSentMessage)
                else
                    _personalDataEvent.emit(PersonalDataEvent.ShowNameChangingFailedMessage(task.exception))
                _isLoading.value = false
            }
        }
    }

    sealed class PersonalDataEvent {
        object NavigateToChangeNameFragment : PersonalDataEvent()
        object ShowPasswordRestEmailSentMessage : PersonalDataEvent()
        data class ShowNameChangingFailedMessage(val exception: Exception?) : PersonalDataEvent()
    }
}