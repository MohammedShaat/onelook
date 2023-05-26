package com.example.onelook.ui.mainactivity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.data.ApplicationLaunchStateManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val appLaunchStateManager: ApplicationLaunchStateManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val hasBeenChecked = state.getLiveData("checked", false)

    private val _isChecking = MutableStateFlow(true)
    val isChecking = _isChecking.asStateFlow()

    fun onCheckAppLaunchStateAndSigning() = flow {
        if (hasBeenChecked.value == true) {
            _isChecking.emit(false)
            return@flow
        }
        hasBeenChecked.value = true

        val isFirstLaunch = appLaunchStateManager.isFirstLaunch()
        val isSignedIn = auth.currentUser?.let { true } ?: false

        when {
            isSignedIn -> emit(MainActivityEvent.NavigateToHomeFragment)
            !isFirstLaunch -> emit(MainActivityEvent.NavigateToLoginFragment)
            else -> _isChecking.emit(false)
        }
    }

    sealed class MainActivityEvent {
        object NavigateToLoginFragment : MainActivityEvent()
        object NavigateToHomeFragment : MainActivityEvent()
    }
}