package com.example.onelook.authentication.presentation.signup

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.authentication.data.remote.UserApi
import com.example.onelook.authentication.domain.repository.AuthenticationRepository
import com.example.onelook.common.data.repository.AppState
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.common.util.Resource
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val appStateRepositoryImpl: AppStateRepositoryImpl,
    private val userApi: UserApi,
    val auth: FirebaseAuth,
    private val authenticationRepository: AuthenticationRepository,
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

    // marks that the app has been launched in DataStore
    fun onSignUpVisited() = viewModelScope.launch {
        appStateRepositoryImpl.updateAppState(AppState.LOGGED_OUT)
    }

    fun onErrorOccurred(message: String? = null) = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.ErrorOccurred(message))
    }

    fun onPasswordVisibilityClicked() {
        _passwordVisibility.value = !_passwordVisibility.value!!
    }

    fun onButtonLoginClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.NavigateToLoginFragment)
    }

    fun onButtonSignUpWithEmailClicked() = viewModelScope.launch {
        resetErrors()
        _singUpEvent.emit(SignUpEvent.HideErrors)
        val name = name.value!!
        val email = email.value!!
        val password = password.value!!

        _errorFields = inputsAreNotEmpty(name, email, password)
        if (_errorFields.isNotEmpty()) {
            _singUpEvent.emit(SignUpEvent.ShowEmptyFieldsMessage)
            return@launch
        }

        isLoading(true)
        val result = authenticationRepository.signUpWithEmailAndPassword(email, password, name)
        if (result is Resource.Success) {
            _singUpEvent.emit(SignUpEvent.NavigateToHomeFragment)
        } else {
            creationWithEmailFailed(result.exception)
        }
        isLoading(false)

    }

    fun onButtonSignUpWithGoogleClicked() = viewModelScope.launch {
        resetErrors()
        _singUpEvent.emit(SignUpEvent.HideErrors)
        isLoading(true)
        _singUpEvent.emit(SignUpEvent.SignUpWithGoogle)
    }

    fun onButtonSignUpWithFacebookClicked() = viewModelScope.launch {
        resetErrors()
        _singUpEvent.emit(SignUpEvent.HideErrors)
        isLoading(true)
        _singUpEvent.emit(SignUpEvent.SignUpWithFacebook)
    }

    private fun creationWithEmailFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Timber.e("PasswordWeak: $exception")
                _errorFields = listOf(Fields.PASSWORD)
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.WEAK_PASSWORD,
                        message = exception.reason
                    )
                )
            }

            is FirebaseAuthInvalidCredentialsException -> {
                Timber.e("EmailInvalid: $exception")
                _errorFields = listOf(Fields.EMAIL)
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.INVALID_EMAIL
                    )
                )
            }

            is FirebaseAuthUserCollisionException -> {
                Timber.e("Existing account: $exception")
                _errorFields = listOf(Fields.EMAIL)
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.EXISTING_EMAIL
                    )
                )
            }

            is FirebaseNetworkException -> {
                Timber.e("network error: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.NETWORK_ISSUE
                    )
                )
            }

            is HttpException -> {
                if (exception.code() == HttpURLConnection.HTTP_CONFLICT)
                    _singUpEvent.emit(
                        SignUpEvent.ShowCreationWithEmailFailedMessage(
                            CreationWithEmailExceptions.EXISTING_EMAIL
                        )
                    )
                else
                    _singUpEvent.emit(
                        SignUpEvent.ShowCreationWithEmailFailedMessage(
                            CreationWithEmailExceptions.OTHER_EXCEPTIONS,
                            message = exception.localizedMessage
                        )
                    )
            }

            else -> {
                Timber.e("user creation failed\n $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.OTHER_EXCEPTIONS,
                        message = exception?.localizedMessage
                    )
                )
            }
        }
    }

    private fun creationWithProviderFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseNetworkException -> {
                Timber.e("network error: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithProviderFailedMessage(
                        CreationWithProviderExceptions.NETWORK_ISSUE,
                        exception.localizedMessage
                    )
                )
            }

            is HttpException -> {
                if (exception.code() == HttpURLConnection.HTTP_CONFLICT)
                    _singUpEvent.emit(
                        SignUpEvent.ShowCreationWithProviderFailedMessage(
                            CreationWithProviderExceptions.EXISTING_EMAIL,
                        )
                    )
                else
                    _singUpEvent.emit(
                        SignUpEvent.ShowCreationWithProviderFailedMessage(
                            CreationWithProviderExceptions.OTHER_EXCEPTIONS,
                            message = exception.localizedMessage
                        )
                    )
            }

            else -> {
                Timber.e("user creation failed\n $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithProviderFailedMessage(
                        CreationWithProviderExceptions.OTHER_EXCEPTIONS,
                        message = exception?.localizedMessage
                    )
                )
            }
        }
    }

    private fun inputsAreNotEmpty(
        name: String,
        email: String,
        password: String
    ): List<Fields> {
        val fields = mutableListOf<Fields>()
        if (name.isBlank())
            fields.add(Fields.NAME)
        if (email.isBlank())
            fields.add(Fields.EMAIL)
        if (password.isBlank())
            fields.add(Fields.PASSWORD)
        return fields
    }

    fun onGoogleTokenReceived(credential: SignInCredential) = viewModelScope.launch {
        isLoading(true)
        val googleAuthCredential = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        val result =
            authenticationRepository.signUpWithCredential(googleAuthCredential, credential.displayName)
        if (result is Resource.Success) {
            _singUpEvent.emit(SignUpEvent.NavigateToHomeFragment)
        } else {
            creationWithProviderFailed(result.exception)
        }
        isLoading(false)
    }

    fun onFacebookTokenReceived(accessToken: AccessToken) = viewModelScope.launch {
        isLoading(true)
        val facebookCredential = FacebookAuthProvider.getCredential(accessToken.token)
        val data = getUserNameAndEmailFromFacebook(accessToken)
        val result =
            authenticationRepository.signUpWithCredential(facebookCredential, data.first, data.second)
        if (result is Resource.Success) {
            _singUpEvent.emit(SignUpEvent.NavigateToHomeFragment)
        } else {
            creationWithProviderFailed(result.exception)
        }
        isLoading(false)
    }

    private suspend fun getUserNameAndEmailFromFacebook(accessToken: AccessToken?): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            var name: String? = null
            var email: String? = null

            val request = GraphRequest.newMeRequest(accessToken) { jsonObj, _ ->
                name = jsonObj?.getString("name")
                email =
                    if (jsonObj?.has("email") == true) jsonObj.getString("email")
                    else null
                Timber.i("request: name: $name, email: $email")
            }
            val parameters = Bundle()
            parameters.putString("fields", "email,name")
            request.parameters = parameters
            request.executeAndWait()
            Pair(name, email)
        }
    }

    private fun resetErrors() {
        _errorFields = emptyList()
        _errorMessage = ""
    }


    sealed class SignUpEvent {
        object HideErrors : SignUpEvent()
        object ShowEmptyFieldsMessage : SignUpEvent()
        data class ShowCreationWithEmailFailedMessage(
            val exception: CreationWithEmailExceptions,
            val message: String? = null,
        ) : SignUpEvent()

        data class ShowCreationWithProviderFailedMessage(
            val exception: CreationWithProviderExceptions,
            val message: String? = null
        ) : SignUpEvent()

        object NavigateToLoginFragment : SignUpEvent()
        object NavigateToHomeFragment : SignUpEvent()
        object SignUpWithGoogle : SignUpEvent()
        object SignUpWithFacebook : SignUpEvent()
        data class ErrorOccurred(val message: String? = null) : SignUpEvent()
    }

    enum class Fields {
        NAME, EMAIL, PASSWORD
    }

    enum class CreationWithEmailExceptions {
        WEAK_PASSWORD, EXISTING_EMAIL, INVALID_EMAIL, NETWORK_ISSUE, OTHER_EXCEPTIONS
    }

    enum class CreationWithProviderExceptions {
        EXISTING_EMAIL, NETWORK_ISSUE, OTHER_EXCEPTIONS
    }
}