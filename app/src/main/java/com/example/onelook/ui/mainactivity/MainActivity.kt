package com.example.onelook.ui.mainactivity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.onelook.GLOBAL_TAG
import com.example.onelook.R
import com.example.onelook.util.onCollect
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var navController: NavController
    var keepSplashScreen = true

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
        supportActionBar?.hide()
        setupNavigation()

        // Observers
        viewModel.apply {

            onCollect(isChecking) { isChecking ->
                keepSplashScreen = isChecking
            }

            onCollect(checkAppLaunchStateAndSigningFlow) { event ->
                when (event) {
                    is MainActivityViewModel.MainActivityEvent.NavigateToLoginFragment -> {
                        Timber.tag(GLOBAL_TAG).i("NavigateToLoginFragment")
                        navController.popBackStack()
                        navController.navigate(R.id.loginFragment)
                    }
                    is MainActivityViewModel.MainActivityEvent.NavigateToHomeFragment -> {
                        Timber.tag(GLOBAL_TAG).i("NavigateToHomeFragment")
                        navController.popBackStack()
                        navController.navigate(R.id.homeFragment)
                    }
                }
            }
        }//Observers
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

    fun hideSplashScreen() {
        keepSplashScreen = false
    }
}