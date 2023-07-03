package com.example.onelook.ui.mainactivity

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.onelook.data.AppState
import com.example.onelook.data.AppStateManager
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.ui.timer.TimerFragment
import com.example.onelook.util.ACTION_OPEN_TIMER
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    state: SavedStateHandle,
    private val appStateManager: AppStateManager,
    @ApplicationContext private val context: Context,
    private val repository: Repository
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
        when {
            appState == AppState.FIRST_LAUNCH -> _isChecking.emit(false)

            intent.action == ACTION_OPEN_TIMER -> emit(
                MainActivityEvent.NavigateToTimerFragment(intent.getParcelableExtra("activity_history"))
            )

            appState == AppState.LOGGED_IN -> emit(MainActivityEvent.NavigateToHomeFragment)

            appState == AppState.LOGGED_OUT -> emit(MainActivityEvent.NavigateToLoginFragment)
        }

        hasBeenChecked.value = true

        // Sync
        if (appState == AppState.LOGGED_IN) {
            repository.sync().collect()
        }
    }

    sealed class MainActivityEvent {
        object NavigateToLoginFragment : MainActivityEvent()
        object NavigateToHomeFragment : MainActivityEvent()
        data class NavigateToTimerFragment(val activityHistory: ActivityHistory?) :
            MainActivityEvent()
    }
}