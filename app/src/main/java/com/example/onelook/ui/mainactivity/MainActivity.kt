package com.example.onelook.ui.mainactivity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.onelook.GLOBAL_TAG
import com.example.onelook.R
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

    // Just for testing
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.fragment_login)
//        supportActionBar?.hide()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkAppLaunchStateAndSigningFlow = viewModel.onCheckAppLaunchStateAndSigning()
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                keepSplashScreen
            }
        }

        setContentView(R.layout.activity_main)
        setupNavController()
        setupAndHandleBottomNavigation()
        hideBottomNavigation()

        // Observers
        viewModel.apply {

            onCollect(isChecking) { isChecking ->
                keepSplashScreen = isChecking
            }

            onCollect(checkAppLaunchStateAndSigningFlow) { event ->
                when (event) {
                    is MainActivityViewModel.MainActivityEvent.NavigateToLoginFragment -> {
                        navController.popBackStack()
                        navController.navigate(R.id.loginFragment)
                    }
                    is MainActivityViewModel.MainActivityEvent.NavigateToHomeFragment -> {
                        navController.popBackStack()
                        navController.navigate(R.id.homeFragment)
                    }
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
                            navController.navigate(R.id.homeFragment)
                            true
                        }
                        R.id.action_timer -> {
                            true
                        }
                        R.id.action_progress -> {
                            true
                        }
                        R.id.action_settings -> {
                            navController.popBackStack()
                            navController.navigate(R.id.settingsFragment)
                            true
                        }
                        else -> false
                    }
                }

                // To prevent repeated navigation to the same destination
                setOnItemReselectedListener {}
            }
    }

    fun setSelectedItem(@IdRes id: Int) {
        bottomNavigationView.selectedItemId = id
    }

    fun showBottomNavigation() {
        bottomNavigationView.isVisible = true
    }

    fun hideBottomNavigation() {
        bottomNavigationView.isVisible = false
    }
}