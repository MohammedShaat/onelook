package com.example.onelook.ui.mainactivity

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.onelook.data.AppState
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.ui.timer.TimerFragment
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

    fun onCheckAppLaunchStateAndSigning(intent: Intent) = flow {
        if (hasBeenChecked.value == true) {
            _isChecking.emit(false)
            return@flow
        }

        val appState = appStateManager.getAppState()
        val fragmentToOpen = intent.getStringExtra("fragment_to_open")
        when {
            appState == AppState.FIRST_LAUNCH -> _isChecking.emit(false)
            fragmentToOpen != null && fragmentToOpen == TimerFragment::class.java.name ->
                emit(
                    MainActivityEvent.NavigateToTimerFragment(
                        intent.getParcelableExtra(
                            "activity_history"
                        )
                    )
                )
            appState == AppState.LOGGED_IN -> emit(MainActivityEvent.NavigateToHomeFragment)
            appState == AppState.LOGGED_OUT -> emit(MainActivityEvent.NavigateToLoginFragment)
        }
        hasBeenChecked.value = true
    }

    sealed class MainActivityEvent {
        object NavigateToLoginFragment : MainActivityEvent()
        object NavigateToHomeFragment : MainActivityEvent()
        data class NavigateToTimerFragment(val activityHistory: ActivityHistory?) :
            MainActivityEvent()
    }
}