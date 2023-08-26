package com.example.onelook.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.onelook.R
import com.example.onelook.tasks.presentation.home.HomeFragment
import com.example.onelook.common.presentation.MainActivity
import com.example.onelook.profile_and_settings.presentation.settings.SettingsFragment
import com.example.onelook.timer.presentation.TimerFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

fun <T> Fragment.onCollect(flow: Flow<T>, block: (value: T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        flow.collect(block)
    }
}

fun <T> AppCompatActivity.onCollect(flow: Flow<T>, block: (value: T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(block)
        }
    }
}

fun Fragment.isInternetAvailable(): Boolean {
    val connectivityManager =
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val network = connectivityManager.activeNetworkInfo ?: return false
        return network.isConnected
    }
}

fun Fragment.hideBottomNavigation() {
    mainActivity.hideBottomNavigation()
}

fun Fragment.showBottomNavigation() {
    mainActivity.showBottomNavigation()
    val menu = mainActivity.bottomNavigationView.menu
    when (this) {
        is HomeFragment -> menu.findItem(R.id.action_home).isChecked = true
        is TimerFragment -> menu.findItem(R.id.action_timer).isChecked = true
        is SettingsFragment -> menu.findItem(R.id.action_settings).isChecked = true
    }
}

fun Fragment.hideSplashScreen() {
    (requireActivity() as MainActivity).hideSplashScreen()
}

val Fragment.mainActivity: MainActivity
    get() = requireActivity() as MainActivity

fun Fragment.enableDoubleBackClick() {
    mainActivity.onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            private var lastBackClickMillis = 0L
            override fun handleOnBackPressed() {
                val currentTime = Calendar.getInstance().timeInMillis
                if (currentTime - lastBackClickMillis <= DOUBLE_BACK_INTERVAL) {
                    isEnabled = false
                    mainActivity.onBackPressed()
                } else {
                    lastBackClickMillis = currentTime
                    Toast.makeText(context, R.string.click_back_again, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}