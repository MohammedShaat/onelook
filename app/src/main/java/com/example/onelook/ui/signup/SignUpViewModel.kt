package com.example.onelook.ui.signup

import android.os.Bundle
import androidx.lifecycle.*
import com.example.onelook.data.ApplicationLaunchStateManager
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val name = name.value!!
        val email = email.value!!
        val password = password.value!!

        val emptyFields = inputsAreNotEmpty(name, email, password)
        if (emptyFields.isNotEmpty()) {
            _singUpEvent.emit(SignUpEvent.ShowEmptyFieldsMessage(emptyFields))
            return@launch
        }

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    creationWithEmailOrProviderSucceeded(name)
                } else {
                    _isLoading.value = false
                    creationWithEmailFailed(task.exception)
                }
            }
    }

    fun onButtonSignUpWithGoogleClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.SignUpWithGoogle)
    }

    fun onButtonSignUpWithFacebookClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.SignUpWithFacebook)
    }

    private fun creationWithEmailFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Timber.e("PasswordWeak: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.WEAK_PASSWORD,
                        fields = listOf(Fields.PASSWORD),
                        message = exception.reason
                    )
                )
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Timber.e("EmailInvalid: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.INVALID_EMAIL
                    )
                )
            }
            is FirebaseAuthUserCollisionException -> {
                Timber.e("Existing account: $exception")
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
            else -> {
                Timber.e("user creation failed\n $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.OTHER_EXCEPTIONS
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
                        CreationWithProviderExceptions.NETWORK_ISSUE
                    )
                )
            }
            else -> {
                Timber.e("user creation failed\n $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithProviderFailedMessage(
                        CreationWithProviderExceptions.OTHER_EXCEPTIONS
                    )
                )
            }
        }
    }

    private fun creationWithEmailOrProviderSucceeded(name: String?) {
        val user = auth.currentUser!!
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(request).addOnCompleteListener {
            viewModelScope.launch {
                _isLoading.value = false
                _singUpEvent.emit(SignUpEvent.NavigateToHomeFragment)
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

    fun onGoogleTokenReceived(credential: SignInCredential) {
        val googleAuthCredential = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        _isLoading.value = true
        auth.signInWithCredential(googleAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                creationWithEmailOrProviderSucceeded(credential.displayName)
            } else {
                creationWithProviderFailed(task.exception)
                _isLoading.value = false
            }
        }
    }

    fun onFacebookTokenReceived(accessToken: AccessToken) {
        _isLoading.value = true
        val facebookCredential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(facebookCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) viewModelScope.launch {
                val name = getUserNameFromFacebook(accessToken)
                creationWithEmailOrProviderSucceeded(name)
            } else {
                creationWithProviderFailed(task.exception)
                _isLoading.value = false
            }
        }
    }

    private suspend fun getUserNameFromFacebook(accessToken: AccessToken?): String? {
        return withContext(Dispatchers.IO) {
            var name: String? = null

            val request = GraphRequest.newMeRequest(accessToken) { jsonObj, _ ->
                name = jsonObj?.getString("name")
            }
            val parameters = Bundle()
            parameters.putString("fields", "name")
            request.parameters = parameters
            request.executeAndWait()

            name
        }
    }

    sealed class SignUpEvent {
        data class ShowEmptyFieldsMessage(val fields: List<Fields>) : SignUpEvent()
        data class ShowCreationWithEmailFailedMessage(
            val exception: CreationWithEmailExceptions,
            val fields: List<Fields> = emptyList(),
            val message: String? = null,
        ) : SignUpEvent()

        data class ShowCreationWithProviderFailedMessage(
            val exception: CreationWithProviderExceptions
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
        NETWORK_ISSUE, OTHER_EXCEPTIONS
    }
}