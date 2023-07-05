package com.example.onelook.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.onelook.ui.mainactivity.MainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
    (requireActivity() as MainActivity).hideBottomNavigation()
}

fun Fragment.showBottomNavigation() {
    (requireActivity() as MainActivity).showBottomNavigation()
}

fun Fragment.hideSplashScreen() {
    (requireActivity() as MainActivity).hideSplashScreen()
}

val Fragment.mainActivity: MainActivity
    get() = requireActivity() as MainActivity