package com.example.onelook.ui.signup

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.example.onelook.data.ApplicationLaunchStateManager
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
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

        val emptyFields = inputsAreNotEmpty(name, email, password)
        if (emptyFields.isNotEmpty()) {
            _singUpEvent.emit(SignUpEvent.ShowEmptyFieldsMessage(emptyFields))
            return@launch
        }

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    creationWithEmailOrProviderSucceeded(name)
                else
                    creationWithEmailFailed(task.exception)
                _isLoading.value = false
            }
    }

    private fun creationWithEmailOrProviderSucceeded(name: String?) {
//        Timber.i("user creation with email succeeded")
        val user = auth.currentUser!!
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(request).addOnCompleteListener {
            viewModelScope.launch {
                _singUpEvent.emit(SignUpEvent.NavigateToHomeFragment)
            }
        }
    }

    private fun creationWithEmailFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Timber.i("PasswordWeak: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.WEAK_PASSWORD,
                        fields = listOf(Fields.PASSWORD),
                        message = exception.reason
                    )
                )
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Timber.i("EmailInvalid: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.INVALID_EMAIL
                    )
                )
            }
            is FirebaseAuthUserCollisionException -> {
                Timber.i("Existing account: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.EXISTING_EMAIL
                    )
                )
            }
            is FirebaseNetworkException -> {
                Timber.i("network error: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.NETWORK_ISSUE
                    )
                )
            }
            else -> {
                Timber.i("user creation failed\n $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithEmailFailedMessage(
                        CreationWithEmailExceptions.OTHER_EXCEPTIONS
                    )
                )
            }
        }
    }

    // Returns EmptyFields contains list of fields or empty if there are no empty fields
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

    // Enables/disable password visibility
    fun onPasswordVisibilityClicked() {
        _passwordVisibility.value = !_passwordVisibility.value!!
    }

    // Sends NavigateToLoginFragment event
    fun onLoginClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.NavigateToLoginFragment)
    }

    fun onButtonSignUpWithGoogleClicked() = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.SignUpWithGoogle)
    }

    fun onSignInCredentialReceived(credential: SignInCredential) {
        val googleAuthCredential = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        _isLoading.value = true
        auth.signInWithCredential(googleAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful)
                creationWithEmailOrProviderSucceeded(credential.displayName)
            else
                creationWithProviderFailed(task.exception)
            _isLoading.value = false
        }
    }

    private fun creationWithProviderFailed(exception: Exception?) = viewModelScope.launch {
        when (exception) {
            is FirebaseNetworkException -> {
                Timber.i("network error: $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithProviderFailedMessage(
                        CreationWithProviderExceptions.NETWORK_ISSUE
                    )
                )
            }
            else -> {
                Timber.i("user creation failed\n $exception")
                _singUpEvent.emit(
                    SignUpEvent.ShowCreationWithProviderFailedMessage(
                        CreationWithProviderExceptions.OTHER_EXCEPTIONS
                    )
                )
            }
        }
    }

    fun onErrorOccurred(id: Int? = null) = viewModelScope.launch {
        _singUpEvent.emit(SignUpEvent.ErrorOccurred(id))
    }

    sealed class SignUpEvent {
        object HideErrors : SignUpEvent()
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
        data class ErrorOccurred(@StringRes val id: Int? = null) : SignUpEvent()
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