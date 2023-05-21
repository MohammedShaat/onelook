package com.example.onelook.ui.login

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    val email = state.getLiveData("email", "")
    val password = state.getLiveData("password", "")

    private val _passwordVisibility = MutableLiveData(false)
    val passwordVisibility: LiveData<Boolean>
        get() = _passwordVisibility

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    // Logins using an existing user account
    fun onButtonLoginClicked() = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.HideErrors)
        val emptyFields = inputsAreValid(email.value!!, password.value!!)
        if (emptyFields.fields.isNotEmpty())
            _loginEvent.emit(emptyFields)
    }

    // Returns EmptyFields contains list of fields or empty if there are no empty fields
    private fun inputsAreValid(email: String, password: String): LoginEvent.EmptyFields {
        val fields = mutableListOf<LoginEvent.Fields>()
        if (email.isBlank())
            fields.add(LoginEvent.Fields.EMAIL)
        if (password.isBlank())
            fields.add(LoginEvent.Fields.PASSWORD)
        return LoginEvent.EmptyFields(fields)
    }

    // Enables/disable password visibility
    fun onPasswordVisibilityClicked() {
        _passwordVisibility.value = !_passwordVisibility.value!!
    }

    // Sends NavigateToPasswordReminder1Fragment event
    fun onForgetPasswordClicked() = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.NavigateToPasswordReminder1Fragment)
    }

    // Sends NavigateToSignUpFragment event
    fun onSignUpClicked() = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.NavigateToSignUpFragment)
    }

    sealed class LoginEvent {
        object HideErrors : LoginEvent()
        data class EmptyFields(val fields: List<Fields>) : LoginEvent()
        enum class Fields {
            EMAIL, PASSWORD
        }
        object NavigateToPasswordReminder1Fragment : LoginEvent()
        object NavigateToSignUpFragment : LoginEvent()
    }
}