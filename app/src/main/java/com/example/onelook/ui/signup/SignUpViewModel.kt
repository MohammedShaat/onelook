package com.example.onelook.ui.signup

import androidx.lifecycle.*
import com.example.onelook.data.ApplicationLaunchStateManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
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

    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // marks that the app has been launched in DataStore
    fun onSignUpVisited() = viewModelScope.launch {
        appLaunchStateManager.updateApplicationLaunchState()
    }

    // Creates a new user account
    fun onButtonSignUpWithEmailClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.HideErrors)

        val name = name.value!!
        val email = email.value!!
        val password = password.value!!

        val emptyFields = inputsAreValid(name, email, password)
        if (emptyFields.fields.isNotEmpty()) {
            _singUpEvent.emit(emptyFields)
            return@launch
        }

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    userCreationWithEmailSucceeded(name)
                else
                    userCreationWithEmailFailed(task.exception)
                _isLoading.value = false
            }
    }

    private fun userCreationWithEmailSucceeded(name: String) {
        Timber.i("user creation with email succeeded")
        val user = auth.currentUser!!
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(request).addOnCompleteListener { task ->
            if (task.isSuccessful) viewModelScope.launch {
                _singUpEvent.emit(SignUpEvent.NavigateToHomeFragment)
            }
        }
    }

    private fun userCreationWithEmailFailed(exception: Exception?) = viewModelScope.launch {
        Timber.i("user creation with email failed")
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Timber.i("PasswordWeak: $exception")
                _singUpEvent.emit(SignUpEvent.WeakPasswordException(exception.reason))
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Timber.i("EmailInvalid: $exception")
                _singUpEvent.emit(SignUpEvent.InvalidEmailException)
            }
            is FirebaseAuthUserCollisionException -> {
                Timber.i("Existing account: $exception")
                _singUpEvent.emit(SignUpEvent.ExistingEmailException)
            }
            is FirebaseNetworkException -> {
                Timber.i("network error: $exception")
                _singUpEvent.emit(SignUpEvent.NetworkException)
            }
            else -> {
                Timber.i("user creation failed\n $exception\n\n")
                _singUpEvent.emit(SignUpEvent.UnexpectedException)
            }
        }
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

        object NavigateToLoginFragment : SignUpEvent()
        data class WeakPasswordException(val reason: String?) : SignUpEvent()
        object ExistingEmailException : SignUpEvent()
        object InvalidEmailException : SignUpEvent()
        object NetworkException : SignUpEvent()
        object UnexpectedException : SignUpEvent()
        object NavigateToHomeFragment : SignUpEvent()
    }
}