package com.example.onelook

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.onelook.data.ApplicationLaunchStateManager
import com.example.onelook.ui.welcome.WelcomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
        supportActionBar?.hide()

        // Navigate to Sign Up screen if it's not first launch
        val isFirstLaunch = intent.getBooleanExtra(IS_FIRST_LAUNCH_EXTRA_NAME, false)
        if (!isFirstLaunch) {
            val action = WelcomeFragmentDirections.actionWelcomeFragmentToSignUpFragment()
            navController.navigate(action)
        }
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
}