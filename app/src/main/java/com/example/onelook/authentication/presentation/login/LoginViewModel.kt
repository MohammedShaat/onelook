package com.example.onelook.authentication.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.authentication.domain.repository.AuthenticationRepository
import com.example.onelook.common.util.Resource
import com.facebook.AccessToken
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    state: SavedStateHandle,
    private val authenticationRepository: AuthenticationRepository,
) : ViewModel() {

    val email = state.getLiveData("email", "")
    val password = state.getLiveData("password", "")

    private val _passwordVisibility = MutableLiveData(false)
    val passwordVisibility: LiveData<Boolean>
        get() = _passwordVisibility

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val singUpEvent = _loginEvent.asSharedFlow()

    private var _errorMessage = ""
    val errorMessage: String
        get() = _errorMessage

    private var _errorFields = emptyList<Fields>()
    val errorFields: List<Fields>
        get() = _errorFields

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun isLoading(status: Boolean) = viewModelScope.launch {
        _isLoading.value = status
    }

    fun onErrorMessageChanged(message: String) {
        _errorMessage = message
    }

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
        _loginEvent.emit(LoginEvent.NavigateToPasswordReminder)
    }

    fun onButtonLoginWithEmailClicked() = viewModelScope.launch {
        resetErrors()
        _loginEvent.emit(LoginEvent.HideErrors)
        val email = email.value!!
        val password = password.value!!

        _errorFields = inputsAreNotEmpty(email, password)
        if (_errorFields.isNotEmpty()) {
            _loginEvent.emit(LoginEvent.ShowEmptyFieldsMessage)
            return@launch
        }

        isLoading(true)
        val result = authenticationRepository.signInWithEmailAndPassword(email = email, password = password)
        if (result is Resource.Success) {
            _loginEvent.emit(LoginEvent.NavigateToHomeFragment)
        } else {
            signingWithEmailFailed(result.exception)
        }
        isLoading(false)
    }

    fun onButtonLoginWithGoogleClicked() = viewModelScope.launch {
        resetErrors()
        _loginEvent.emit(LoginEvent.HideErrors)
        isLoading(true)
        _loginEvent.emit(LoginEvent.LoginWithGoogle)
    }

    fun onButtonLoginWithFacebookClicked() = viewModelScope.launch {
        resetErrors()
        _loginEvent.emit(LoginEvent.HideErrors)
        isLoading(true)
        _loginEvent.emit(LoginEvent.LoginWithFacebook)
    }

    private fun signingWithEmailFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                Timber.e("Email not found\n $exception")
                _errorFields = listOf(Fields.EMAIL)
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithEmailFailedMessage(SigningWithEmailExceptions.NO_EXIST_USER)
                )
            }

            is FirebaseAuthInvalidCredentialsException -> {
                Timber.e("Password is wrong\n $exception")
                _errorFields = listOf(Fields.PASSWORD)
                _loginEvent.emit(
                    LoginEvent.ShowSigningWithEmailFailedMessage(SigningWithEmailExceptions.WRONG_PASSWORD)
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

            is HttpException -> {
                if (exception.code() == HttpURLConnection.HTTP_NOT_FOUND)
                    _loginEvent.emit(
                        LoginEvent.ShowSigningWithEmailFailedMessage(
                            SigningWithEmailExceptions.NO_EXIST_USER,
                        )
                    )
                else
                    _loginEvent.emit(
                        LoginEvent.ShowSigningWithEmailFailedMessage(
                            SigningWithEmailExceptions.OTHER_EXCEPTIONS,
                            message = exception.localizedMessage
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

            is HttpException -> {
                if (exception.code() == HttpURLConnection.HTTP_NOT_FOUND)
                    _loginEvent.emit(
                        LoginEvent.ShowSigningWithProviderFailedMessage(
                            SigningWithProviderExceptions.NO_EXIST_USER,
                        )
                    )
                else
                    _loginEvent.emit(
                        LoginEvent.ShowSigningWithProviderFailedMessage(
                            SigningWithProviderExceptions.OTHER_EXCEPTIONS,
                            message = exception.localizedMessage
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

    fun onGoogleTokenReceived(credential: SignInCredential) = viewModelScope.launch {
        isLoading(true)
        val googleAuthCredential = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        val result = authenticationRepository.signInWithCredential(googleAuthCredential)
        if (result is Resource.Success) {
            _loginEvent.emit(LoginEvent.NavigateToHomeFragment)
        } else {
            signingWithProviderFailed(result.exception)
        }
        isLoading(false)
    }

    fun onFacebookTokenReceived(accessToken: AccessToken) = viewModelScope.launch {
        isLoading(true)
        val facebookCredential = FacebookAuthProvider.getCredential(accessToken.token)
        val result = authenticationRepository.signInWithCredential(facebookCredential)
        if (result is Resource.Success) {
            _loginEvent.emit(LoginEvent.NavigateToHomeFragment)
        } else {
            signingWithProviderFailed(result.exception)
        }
        isLoading(false)
    }

    sealed class LoginEvent {
        object HideErrors : LoginEvent()
        object ShowEmptyFieldsMessage : LoginEvent()
        data class ShowSigningWithEmailFailedMessage(
            val exception: SigningWithEmailExceptions,
            val message: String? = null,
        ) : LoginEvent()

        data class ShowSigningWithProviderFailedMessage(
            val exception: SigningWithProviderExceptions,
            val message: String? = null
        ) : LoginEvent()

        object NavigateToSignUpFragment : LoginEvent()
        object NavigateToPasswordReminder : LoginEvent()
        object NavigateToHomeFragment : LoginEvent()
        object LoginWithGoogle : LoginEvent()
        object LoginWithFacebook : LoginEvent()
        data class ErrorOccurred(val message: String? = null) : LoginEvent()
    }

    private fun resetErrors() {
        _errorFields = emptyList()
        _errorMessage = ""
    }

    enum class Fields {
        EMAIL, PASSWORD
    }

    enum class SigningWithEmailExceptions {
        NO_EXIST_USER, WRONG_PASSWORD, NETWORK_ISSUE, TOO_MANY_REQUESTS, OTHER_EXCEPTIONS
    }

    enum class SigningWithProviderExceptions {
        NO_EXIST_USER, NETWORK_ISSUE, OTHER_EXCEPTIONS
    }
}