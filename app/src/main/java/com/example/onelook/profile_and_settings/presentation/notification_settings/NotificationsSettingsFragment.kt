package com.example.onelook.profile_and_settings.presentation.notification_settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentNotificationsSettingsBinding
import com.example.onelook.common.util.hideBottomNavigation
import com.example.onelook.common.util.onCollect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsSettingsFragment : Fragment(R.layout.fragment_notifications_settings) {

    private val viewModel: NotificationsSettingsViewModel by viewModels()
    private var _binding: FragmentNotificationsSettingsBinding? = null
    private val binding: FragmentNotificationsSettingsBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        _binding = FragmentNotificationsSettingsBinding.bind(view)


        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            switchAllNotifications.setOnClickListener {
                viewModel.onSwitchAllNotificationsClicked(switchAllNotifications.isChecked)
            }

            switchActivitiesNotifications.setOnClickListener {
                viewModel.onSwitchActivitiesNotificationsClicked(switchActivitiesNotifications.isChecked)
            }

            switchSupplementsNotifications.setOnClickListener {
                viewModel.onSwitchSupplementsNotificationsClicked(switchSupplementsNotifications.isChecked)
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(areAllNotificationsEnabled) { areEnabled ->
                binding.apply {
                    switchAllNotifications.isChecked = areEnabled

                    switchActivitiesNotifications.isEnabled = areEnabled
                    switchSupplementsNotifications.isEnabled = areEnabled
                }
            }

            onCollect(areActivitiesNotificationsEnabled) { areEnabled ->
                binding.switchActivitiesNotifications.isChecked = areEnabled
            }

            onCollect(areSupplementsNotificationsEnabled) { areEnabled ->
                binding.switchSupplementsNotifications.isChecked = areEnabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}