package com.example.onelook.ui.signup

import androidx.lifecycle.*
import com.example.onelook.data.ApplicationLaunchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val appLaunchStateManager: ApplicationLaunchStateManager
) : ViewModel() {

    val name = state.getLiveData("name", "")
    val email = state.getLiveData("email", "")
    val password = state.getLiveData("password", "")
    val buttonSignUpEnabled = state.getLiveData("buttonSignUpEnabled", false)

    private val _passwordVisibility = MutableLiveData(false)
    val passwordVisibility: LiveData<Boolean>
        get() = _passwordVisibility

    private val _singUpEvent = MutableSharedFlow<SignUpEvent>()
    val singUpEvent = _singUpEvent.asSharedFlow()

    // marks that the app has been launched in DataStore
    fun onSignUpVisited() = viewModelScope.launch {
        appLaunchStateManager.updateApplicationLaunchState()
    }

    // Creates a new user account
    fun onButtonSignUpClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.HideErrors)
        val emptyFields = inputsAreValid(name.value!!, email.value!!, password.value!!)
        if (emptyFields.fields.isNotEmpty())
            _singUpEvent.emit(emptyFields)

    }

    // Returns EmptyFields contains list of fields or empty if there are no empty fields
    private fun inputsAreValid(
        name: String,
        email: String,
        password: String
    ): SignUpEvent.EmptyFields {
        val fields = mutableListOf<SignUpEvent.Fields>()
        if (name.isBlank())
            fields.add(SignUpEvent.Fields.NAME)
        if (email.isBlank())
            fields.add(SignUpEvent.Fields.EMAIL)
        if (password.isBlank())
            fields.add(SignUpEvent.Fields.PASSWORD)
        return SignUpEvent.EmptyFields(fields)
    }

    // Enables/disable password visibility
    fun onPasswordVisibilityClicked() {
        _passwordVisibility.value = !_passwordVisibility.value!!
    }

    // Sends NavigateToLoginFragment event
    fun onLoginClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.NavigateToLoginFragment)
    }

    sealed class SignUpEvent {
        object HideErrors : SignUpEvent()
        data class EmptyFields(val fields: List<Fields>) : SignUpEvent()
        enum class Fields {
            NAME, EMAIL, PASSWORD
        }
        object NavigateToLoginFragment: SignUpEvent()
    }
}