package com.example.onelook.common.presentation

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.common.data.repository.AppState
import com.example.onelook.common.data.repository.AppStateRepositoryImpl
import com.example.onelook.timer.data.local.Timer
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.tasks.doamin.model.SupplementHistory
import com.example.onelook.tasks.doamin.repository.TodayTasksRepository
import com.example.onelook.common.util.ACTION_OPEN_ACTIVITY_NOTIFICATION
import com.example.onelook.common.util.ACTION_OPEN_SUPPLEMENT_NOTIFICATION
import com.example.onelook.common.util.ACTION_OPEN_TIMER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    state: SavedStateHandle,
    private val appStateRepositoryImpl: AppStateRepositoryImpl,
    private val todayTasksRepository: TodayTasksRepository,
) : ViewModel() {

    private val hasBeenChecked = state.getLiveData("checked", false)

    private val _isChecking = MutableStateFlow(true)
    val isChecking = _isChecking.asStateFlow()

    fun onCheckAppLaunchStateAndSigning(intent: Intent) = flow {
        if (hasBeenChecked.value == true) {
            _isChecking.emit(false)
            return@flow
        }

        val appState = appStateRepositoryImpl.getAppState()
        when {
            appState == AppState.FIRST_LAUNCH -> _isChecking.emit(false)

            intent.action == ACTION_OPEN_TIMER -> emit(
                MainActivityEvent.NavigateToTimerFragment(intent.getParcelableExtra("activity_history"))
            )

            intent.action == ACTION_OPEN_ACTIVITY_NOTIFICATION &&
                    intent.getParcelableExtra<SupplementHistory>("activity_history") != null -> {
                onNotificationTapped()
                emit(
                    MainActivityEvent.NavigateToTimerFragment(intent.getParcelableExtra("activity_history")!!)
                )
            }

            intent.action == ACTION_OPEN_SUPPLEMENT_NOTIFICATION &&
                    intent.getParcelableExtra<SupplementHistory>("supplement_history") != null -> {
                onNotificationTapped()
                emit(
                    MainActivityEvent.NavigateToSupplementHistoryDetailsFragment(
                        intent.getParcelableExtra(
                            "supplement_history"
                        )!!
                    )
                )
            }

            appState == AppState.LOGGED_IN -> emit(MainActivityEvent.NavigateToHomeFragment)

            appState == AppState.LOGGED_OUT -> emit(MainActivityEvent.NavigateToLoginFragment)
        }

        hasBeenChecked.value = true

        // Sync
        if (appState == AppState.LOGGED_IN && !Timer.isSyncing.value) {
            todayTasksRepository.sync().collect()
        }
    }

    fun onNotificationTapped() = viewModelScope.launch {
        appStateRepositoryImpl.decreaseUnreadNotifications()
    }

    sealed class MainActivityEvent {
        object NavigateToLoginFragment : MainActivityEvent()
        object NavigateToHomeFragment : MainActivityEvent()
        data class NavigateToTimerFragment(val activityHistory: ActivityHistory?) :
            MainActivityEvent()

        data class NavigateToSupplementHistoryDetailsFragment(val supplementHistory: SupplementHistory) :
            MainActivityEvent()
    }
}