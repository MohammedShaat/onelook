package com.example.onelook.ui.signup

import android.text.InputType
import androidx.lifecycle.*
import com.example.onelook.GLOBAL_TAG
import com.example.onelook.data.ApplicationLaunchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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

    private val _inputsErrorMessageEvent = MutableSharedFlow<InputsErrorMessage>()
    val inputsErrorMessageEvent = _inputsErrorMessageEvent.asSharedFlow()

    private val _hideErrorsEvent = MutableSharedFlow<Boolean>()
    val hideErrorsEvent = _hideErrorsEvent.asSharedFlow()

    // marks that the app has been launched in DataStore
    fun onSignUpVisited() = viewModelScope.launch {
        appLaunchStateManager.updateApplicationLaunchState()
    }

    // Enables or disables sign up button when checkbox is clicked
    fun onCheckBoxPrivacyPolicyChanged() {
        buttonSignUpEnabled.value = !buttonSignUpEnabled.value!!
    }

    // Creates a new user account
    fun onButtonSignUpClicked() = viewModelScope.launch {
        _hideErrorsEvent.emit(true)
        val emptyFields = inputsAreValid(name.value!!, email.value!!, password.value!!)
        if (emptyFields.fields.isNotEmpty())
            _inputsErrorMessageEvent.emit(emptyFields)

    }

    //returns EmptyFields contains list of fields or empty if there are no empty fields
    private fun inputsAreValid(
        name: String,
        email: String,
        password: String
    ): InputsErrorMessage.EmptyFields {
        val fields = mutableListOf<InputsErrorMessage.Fields>()
        if (name.isBlank())
            fields.add(InputsErrorMessage.Fields.NAME)
        if (email.isBlank())
            fields.add(InputsErrorMessage.Fields.EMAIL)
        if (password.isBlank())
            fields.add(InputsErrorMessage.Fields.PASSWORD)
        return InputsErrorMessage.EmptyFields(fields)
    }

    fun onPasswordVisibilityClicked() {
        _passwordVisibility.value = !_passwordVisibility.value!!
    }

    sealed class InputsErrorMessage {
        data class EmptyFields(val fields: List<Fields>) : InputsErrorMessage()
        enum class Fields {
            NAME, EMAIL, PASSWORD
        }
    }
}