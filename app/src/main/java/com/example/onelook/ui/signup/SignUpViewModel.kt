package com.example.onelook.ui.signup

import android.os.Bundle
import androidx.lifecycle.*
import com.example.onelook.GLOBAL_TAG
import com.example.onelook.data.AppState
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.data.network.users.UserApi
import com.example.onelook.data.network.users.NetworkUserRegisterRequest
import com.example.onelook.util.toLocalModel
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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val appStateManager: AppStateManager,
    private val userApi: UserApi,
    val auth: FirebaseAuth,
    private val repository: Repository
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

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun isLoading(status: Boolean) = viewModelScope.launch {
        _isLoading.value = status
    }

    // marks that the app has been launched in DataStore
    fun onSignUpVisited() = viewModelScope.launch {
        appStateManager.updateAppState(AppState.LOGGED_OUT)
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

        isLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    creationWithEmailOrProviderSucceeded(name)
                } else {
                    isLoading(false)
                    creationWithEmailFailed(task.exception)
                }
            }
    }

    fun onButtonSignUpWithGoogleClicked() = viewModelScope.launch {
        isLoading(true)
        _singUpEvent.emit(SignUpEvent.SignUpWithGoogle)
    }

    fun onButtonSignUpWithFacebookClicked() = viewModelScope.launch {
        isLoading(true)
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

    private fun creationWithEmailOrProviderSucceeded(
        name: String?, email: String? = null
    ) = viewModelScope.launch {
        val user = auth.currentUser!!
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(request).await()
        if (email != null)
            user.updateEmail(email).await()

        val result = saveAccessToken()
        isLoading(false)
        if (result)
            _singUpEvent.emit(SignUpEvent.NavigateToHomeFragment)
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
        isLoading(true)
        auth.signInWithCredential(googleAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                creationWithEmailOrProviderSucceeded(credential.displayName)
            } else {
                creationWithProviderFailed(task.exception)
                isLoading(false)
            }
        }
    }

    fun onFacebookTokenReceived(accessToken: AccessToken) {
        isLoading(true)
        val facebookCredential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(facebookCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) viewModelScope.launch {
                val data = getUserNameAndEmailFromFacebook(accessToken)
                creationWithEmailOrProviderSucceeded(data.first, data.second)
            } else {
                creationWithProviderFailed(task.exception)
                isLoading(false)
            }
        }
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

    private suspend fun saveAccessToken(): Boolean {
        val user = auth.currentUser!!
        val firebaseToken = user.getIdToken(false).await().token ?: return false
        Timber.i("firebaseToken: $firebaseToken")
        return try {
            val response =
                userApi.register(NetworkUserRegisterRequest(firebaseToken, user.displayName ?: ""))
            repository.loginUserInDatabase(response.user.toLocalModel())
            appStateManager.apply {
                updateAppState(AppState.LOGGED_IN)
                setAccessToken(response.accessToken)
            }
            Timber.i("accessToken: ${response.accessToken}")
            true
        } catch (exception: HttpException) {
            Timber.e("API register failed\n $exception")
            when (exception.code()) {
                HttpURLConnection.HTTP_CONFLICT -> _singUpEvent.emit(SignUpEvent.ShowUserAlreadyExistsMessage)
                else -> _singUpEvent.emit(SignUpEvent.ErrorOccurred(exception.localizedMessage))
            }
            false
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
            val exception: CreationWithProviderExceptions,
            val message: String? = null
        ) : SignUpEvent()

        object NavigateToLoginFragment : SignUpEvent()
        object NavigateToHomeFragment : SignUpEvent()
        object SignUpWithGoogle : SignUpEvent()
        object SignUpWithFacebook : SignUpEvent()
        data class ErrorOccurred(val message: String? = null) : SignUpEvent()
        object ShowUserAlreadyExistsMessage : SignUpEvent()
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