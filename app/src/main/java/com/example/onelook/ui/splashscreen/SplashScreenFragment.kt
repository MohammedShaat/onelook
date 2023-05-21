package com.example.onelook.ui.splashscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.data.ApplicationLaunchStateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    @Inject
    lateinit var appLaunchStateManager: ApplicationLaunchStateManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Check if the app is launched for the first time
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val state = appLaunchStateManager.isFirstLaunch()
            navigate(state, savedInstanceState)
        }
    }

    private fun navigate(isFirstLaunch: Boolean, savedInstanceState: Bundle?) {
//        if (!isFirstLaunch && savedInstanceState == null) {
        if (isFirstLaunch) {
            val action = SplashScreenFragmentDirections.actionSplashScreenFragmentToWelcomeFragment()
            findNavController().navigate(action)
        } else {
            val action = SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment()
            findNavController().navigate(action)
        }
    }
}