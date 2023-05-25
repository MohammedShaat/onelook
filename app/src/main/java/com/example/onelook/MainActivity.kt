package com.example.onelook

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.onelook.data.ApplicationLaunchStateManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    var keepSplashScreen = true
    private var isFirstLaunch = true

    @Inject
    lateinit var appLaunchStateManager: ApplicationLaunchStateManager

    // Just for testing
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.fragment_login)
//        supportActionBar?.hide()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                keepSplashScreen
            }
        }

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        setupNavigation()

        checkAppLaunchStateAndMaybeNavigate()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_Container_view)
                as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    private fun checkAppLaunchStateAndMaybeNavigate() = lifecycleScope.launchWhenStarted {
        val state = appLaunchStateManager.isFirstLaunch()
        isFirstLaunch = state
        if (!isFirstLaunch)
            navigate()
        else
            hideSplashScreen()
    }

    private fun navigate() {
        val destination = when (FirebaseAuth.getInstance().currentUser) {
            null -> R.id.loginFragment
            else -> R.id.homeFragment
        }
        navController.navigate(destination)
    }

    fun hideSplashScreen() {
        keepSplashScreen = false
    }
}