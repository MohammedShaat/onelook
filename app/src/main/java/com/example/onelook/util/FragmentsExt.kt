package com.example.onelook.util

import android.content.Context
import android.content.pm.Capability
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.facebook.gamingservices.cloudgaming.internal.SDKConstants
import kotlinx.coroutines.flow.Flow

fun <T> Fragment.onCollect(flow: Flow<T>, block: (value: T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        flow.collect(block)
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