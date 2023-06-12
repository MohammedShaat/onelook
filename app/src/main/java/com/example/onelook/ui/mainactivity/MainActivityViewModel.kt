package com.example.onelook.ui.mainactivity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.onelook.data.AppState
import com.example.onelook.data.AppStateManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val appStateManager: AppStateManager,
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

        when (appStateManager.getAppState()) {
            AppState.FIRST_LAUNCH -> _isChecking.emit(false)
            AppState.LOGGED_IN -> emit(MainActivityEvent.NavigateToHomeFragment)
            AppState.LOGGED_OUT -> emit(MainActivityEvent.NavigateToLoginFragment)
        }
        hasBeenChecked.value = true
    }

    sealed class MainActivityEvent {
        object NavigateToLoginFragment : MainActivityEvent()
        object NavigateToHomeFragment : MainActivityEvent()
    }
}