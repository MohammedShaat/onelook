package com.example.onelook.ui.mainactivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.onelook.R
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.util.ACTION_OPEN_ACTIVITY_NOTIFICATION
import com.example.onelook.util.ACTION_OPEN_SUPPLEMENT_NOTIFICATION
import com.example.onelook.util.ACTION_OPEN_TIMER
import com.example.onelook.util.ACTIVITIES_TIMER_CHANNEL_ID
import com.example.onelook.util.REMINDERS_CHANNEL_ID
import com.example.onelook.util.onCollect
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var navController: NavController
    private var keepSplashScreen = true
    lateinit var bottomNavigationView: BottomNavigationView

    private var activityHistory: ActivityHistory? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkAppLaunchStateAndSigningFlow = viewModel.onCheckAppLaunchStateAndSigning(intent)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                keepSplashScreen
            }
        }

        setContentView(R.layout.activity_main)
        setupNavController()
        setupAndHandleBottomNavigation()
        hideBottomNavigation()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createNotificationChannel(
                id = ACTIVITIES_TIMER_CHANNEL_ID,
                name = applicationContext.getString(R.string.activities_timer_channel_name),
                importance = NotificationManager.IMPORTANCE_LOW
            )
            createNotificationChannel(
                id = REMINDERS_CHANNEL_ID,
                name = applicationContext.getString(R.string.reminders_channel_name),
                importance = NotificationManager.IMPORTANCE_HIGH,
            )
        }


        // Observers
        viewModel.apply {

            onCollect(isChecking) { isChecking ->
                keepSplashScreen = isChecking
            }

            onCollect(checkAppLaunchStateAndSigningFlow) { event ->
                when (event) {
                    is MainActivityViewModel.MainActivityEvent.NavigateToLoginFragment -> {
                        popAllFragmentsFromBackStack()
                        val action = MainActivityDirections.actionGlobalLoginFragment()
                        navController.navigate(action)
                    }//NavigateToLoginFragment

                    is MainActivityViewModel.MainActivityEvent.NavigateToHomeFragment -> {
                        popAllFragmentsFromBackStack()
                        val action = MainActivityDirections.actionGlobalHomeFragment()
                        navController.navigate(action)
                    }//NavigateToHomeFragment

                    is MainActivityViewModel.MainActivityEvent.NavigateToTimerFragment -> {
                        selectBottomNavigationSettingsItem(event.activityHistory)
                    }//NavigateToTimerFragment

                    is MainActivityViewModel.MainActivityEvent.NavigateToSupplementHistoryDetailsFragment -> {
                        popAllFragmentsFromBackStack()
                        val action =
                            MainActivityDirections.actionGlobalSupplementHistoryDetails(event.supplementHistory)
                        navController.navigate(action)
                    }//NavigateToSupplementHistoryDetailsFragment
                }
            }
        }//Observers
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == ACTION_OPEN_TIMER) {
            val intentActivityHistory =
                intent.getParcelableExtra<ActivityHistory>("activity_history")
            selectBottomNavigationSettingsItem(intentActivityHistory)
            return
        }

        if (intent?.action == ACTION_OPEN_ACTIVITY_NOTIFICATION) {
            val intentActivityHistory =
                intent.getParcelableExtra<ActivityHistory>("activity_history")
            selectBottomNavigationSettingsItem(intentActivityHistory)
            viewModel.onNotificationTapped()
            return
        }

        if (intent?.action == ACTION_OPEN_SUPPLEMENT_NOTIFICATION) {
            val supplementHistory =
                intent.getParcelableExtra<SupplementHistory>("supplement_history") ?: return
            navController.navigate(
                MainActivityDirections.actionGlobalSupplementHistoryDetails(
                    supplementHistory
                )
            )
            viewModel.onNotificationTapped()
            return
        }
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_Container_view)
                as NavHostFragment
        navController = navHostFragment.navController
    }

    fun hideSplashScreen() {
        keepSplashScreen = false
    }

    private fun setupAndHandleBottomNavigation() {
        bottomNavigationView = findViewById<BottomNavigationView?>(R.id.bottom_navigation)
            .apply {
                itemIconTintList = null
                setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.action_home -> {
                            popAllFragmentsFromBackStack()
                            val action = MainActivityDirections.actionGlobalHomeFragment()
                            navController.navigate(action)
                            true
                        }

                        R.id.action_timer -> {
                            val action =
                                MainActivityDirections.actionGlobalTimerFragment(activityHistory)
                            popAllFragmentsFromBackStack()
                            navController.navigate(action)
                            activityHistory = null
                            true
                        }

                        R.id.action_settings -> {
                            popAllFragmentsFromBackStack()
                            val action = MainActivityDirections.actionGlobalSettingsFragment()
                            navController.navigate(action)
                            true
                        }

                        else -> false
                    }
                }

                // To prevent repeated navigation to the same destination
                setOnItemReselectedListener {}
            }
    }

    fun showBottomNavigation() {
        bottomNavigationView.isVisible = true
    }

    fun hideBottomNavigation() {
        bottomNavigationView.isVisible = false
    }

    fun selectBottomNavigationSettingsItem(activityHistory: ActivityHistory?) {
        this.activityHistory = activityHistory
        bottomNavigationView.selectedItemId = R.id.action_timer
    }

    private fun createNotificationChannel(id: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id,
                name,
                importance
            )

            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun cancelCoroutines(cancellationException: CancellationException) {
        viewModel.viewModelScope.coroutineContext.cancelChildren(cancellationException)
    }

    fun popAllFragmentsFromBackStack() {
        val firstFragment = navController.backQueue.first().destination.id
        navController.popBackStack(firstFragment, true)
    }
}