package com.example.onelook.ui.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.onelook.MainActivity
import com.example.onelook.R
import com.example.onelook.data.ApplicationLaunchStateManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var appLaunchStateManager: ApplicationLaunchStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        //Check if the app is launched for the first time
        lifecycleScope.launchWhenStarted {
            Timber.i("before read DataStore")
            val state = appLaunchStateManager.isFinished()
            Timber.i("after read DataStore")
            navigateToMainActivity(state)
        }
    }

    private fun navigateToMainActivity(state: Boolean) {
        val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            .putExtra(IS_FIRST_LAUNCH_EXTRA_NAME, state)
        startActivity(intent)
        finish()
    }

}

const val IS_FIRST_LAUNCH_EXTRA_NAME = "is_first_launch"