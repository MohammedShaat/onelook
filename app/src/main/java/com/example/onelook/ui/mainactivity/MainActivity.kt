package com.example.onelook.ui.mainactivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.onelook.R
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.ui.timer.TimerFragment
import com.example.onelook.util.ACTIVITIES_TIMER_CHANNEL_ID
import com.example.onelook.util.ACTIVITIES_TIMER_CHANNEL_NAME
import com.example.onelook.util.onCollect
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var navController: NavController
    var keepSplashScreen = true
    private lateinit var bottomNavigationView: BottomNavigationView

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
        createNotificationChannel()


        // Observers
        viewModel.apply {

            onCollect(isChecking) { isChecking ->
                keepSplashScreen = isChecking
            }

            onCollect(checkAppLaunchStateAndSigningFlow) { event ->
                when (event) {
                    is MainActivityViewModel.MainActivityEvent.NavigateToLoginFragment -> {
                        navController.popBackStack()
                        val action = MainActivityDirections.actionGlobalLoginFragment()
                        navController.navigate(action)
                    }//NavigateToLoginFragment

                    is MainActivityViewModel.MainActivityEvent.NavigateToHomeFragment -> {
                        navController.popBackStack()
                        val action = MainActivityDirections.actionGlobalHomeFragment()
                        navController.navigate(action)
                    }//NavigateToHomeFragment

                    is MainActivityViewModel.MainActivityEvent.NavigateToTimerFragment -> {
                        selectBottomNavigationSettingsItem(event.activityHistory)
                    }//NavigateToTimerFragment
                }
            }
        }//Observers
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
                            navController.popBackStack()
                            val action = MainActivityDirections.actionGlobalHomeFragment()
                            navController.navigate(action)
                            true
                        }
                        R.id.action_timer -> {
                            navController.popBackStack()
                            val action =
                                MainActivityDirections.actionGlobalTimerFragment(activityHistory)
                            navController.navigate(action)
                            activityHistory = null
                            true
                        }
                        R.id.action_progress -> {
                            true
                        }
                        R.id.action_settings -> {
                            navController.popBackStack()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ACTIVITIES_TIMER_CHANNEL_ID,
                ACTIVITIES_TIMER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}