package com.example.onelook.ui.login

import androidx.lifecycle.*
import com.facebook.AccessToken
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val state: SavedStateHandle,
    val auth: FirebaseAuth
) : ViewModel() {

    val email = state.getLiveData("email", "")
    val password = state.getLiveData("password", "")

    private val _passwordVisibility = MutableLiveData(false)
    val passwordVisibility: LiveData<Boolean>
        get() = _passwordVisibility

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val singUpEvent = _loginEvent.asSharedFlow()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun onErrorOccurred(message: String? = null) = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.ErrorOccurred(message))
    }

    fun onPasswordVisibilityClicked() {
        _passwordVisibility.value = !_passwordVisibility.value!!
    }

    fun onSinUpClick() = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.NavigateToSignUpFragment)
    }

    fun onForgetPasswordClicked() = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.NavigateToPasswordReminder1)
    }

    fun onButtonLoginWithEmailClicked() = viewModelScope.launch {
        val email = email.value!!
        val password = password.value!!

        val emptyFields = inputsAreNotEmpty(email, password)
        if (emptyFields.isNotEmpty()) {
            _loginEvent.emit(LoginEvent.ShowEmptyFieldsMessage(emptyFields))
            return@launch
        }

        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signingWithEmailOrProviderSucceeded()
                } else {
                    _isLoading.value = false
                    signingWithEmailFailed(task.exception)
                }
            }
    }

    fun onButtonLoginWithGoogleClicked() = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.LoginWithGoogle)
    }

    fun onButtonLoginWithFacebookClicked() = viewModelScope.launch {
        _loginEvent.emit(LoginEvent.LoginWithFacebook)
    }

    private fun signingWithEmailFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                Timber.e("Email not found\n $exception")
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithEmailFailedMessage(
                        SigningWithEmailExceptions.NO_EXIST_USER,
                        listOf(Fields.EMAIL)
                    )
                )
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Timber.e("Password is wrong\n $exception")
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithEmailFailedMessage(
                        SigningWithEmailExceptions.WRONG_PASSWORD,
                        listOf(Fields.PASSWORD)
                    )
                )
            }
            is FirebaseNetworkException -> {
                Timber.e("network error: $exception")
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithEmailFailedMessage(
                        SigningWithEmailExceptions.NETWORK_ISSUE
                    )
                )
            }
            is FirebaseTooManyRequestsException -> {
                Timber.e("Too many requests\n $exception")
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithEmailFailedMessage(
                        SigningWithEmailExceptions.TOO_MANY_REQUESTS
                    )
                )
            }
            else -> {
                Timber.e("Signing failed\n $exception")
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithEmailFailedMessage(
                        SigningWithEmailExceptions.OTHER_EXCEPTIONS,
                        message = exception?.localizedMessage
                    )
                )
            }
        }
    }

    private fun signingWithProviderFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseNetworkException -> {
                Timber.e("network error: $exception")
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithProviderFailedMessage(
                        SigningWithProviderExceptions.NETWORK_ISSUE
                    )
                )
            }
            else -> {
                Timber.e("Signing failed\n $exception")
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithProviderFailedMessage(
                        SigningWithProviderExceptions.OTHER_EXCEPTIONS,
                        message = exception?.localizedMessage
                    )
                )
            }
        }
    }

    private fun signingWithEmailOrProviderSucceeded() = viewModelScope.launch {
        _isLoading.value = false
        _loginEvent.emit(LoginEvent.NavigateToHomeFragment)
    }

    private fun inputsAreNotEmpty(
        email: String,
        password: String
    ): List<Fields> {
        val fields = mutableListOf<Fields>()
        if (email.isBlank())
            fields.add(Fields.EMAIL)
        if (password.isBlank())
            fields.add(Fields.PASSWORD)
        return fields
    }

    fun onGoogleTokenReceived(credential: SignInCredential) {
        val googleAuthCredential = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        _isLoading.value = true
        auth.signInWithCredential(googleAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                signingWithEmailOrProviderSucceeded()
            } else {
                signingWithProviderFailed(task.exception)
                _isLoading.value = false
            }
        }
    }

    fun onFacebookTokenReceived(accessToken: AccessToken) {
        _isLoading.value = true
        val facebookCredential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(facebookCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                signingWithEmailOrProviderSucceeded()
            } else {
                signingWithProviderFailed(task.exception)
                _isLoading.value = false
            }
        }
    }

    sealed class LoginEvent {
        data class ShowEmptyFieldsMessage(val fields: List<Fields>) : LoginEvent()
        data class ShowSigningWithEmailFailedMessage(
            val exception: SigningWithEmailExceptions,
            val fields: List<Fields> = emptyList(),
            val message: String? = null,
        ) : LoginEvent()

        data class ShowSigningWithProviderFailedMessage(
            val exception: SigningWithProviderExceptions,
            val message: String? = null
        ) : LoginEvent()

        object NavigateToSignUpFragment : LoginEvent()
        object NavigateToPasswordReminder1 : LoginEvent()
        object NavigateToHomeFragment : LoginEvent()
        object LoginWithGoogle : LoginEvent()
        object LoginWithFacebook : LoginEvent()
        data class ErrorOccurred(val message: String? = null) : LoginEvent()
    }

    enum class Fields {
        EMAIL, PASSWORD
    }

    enum class SigningWithEmailExceptions {
        NO_EXIST_USER, WRONG_PASSWORD, NETWORK_ISSUE, TOO_MANY_REQUESTS, OTHER_EXCEPTIONS
    }

    enum class SigningWithProviderExceptions {
        NETWORK_ISSUE, OTHER_EXCEPTIONS
    }
}