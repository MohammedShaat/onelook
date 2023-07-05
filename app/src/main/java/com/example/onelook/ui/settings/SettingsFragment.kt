package com.example.onelook.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentSettingsBinding
import com.example.onelook.util.onCollect
import com.example.onelook.util.showBottomNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showBottomNavigation()
        _binding = FragmentSettingsBinding.bind(view)

        // Listeners
        binding.apply {

            textViewActivityManager.setOnClickListener {
                viewModel.onActivityManagerClicked()
            }

            textViewPersonalData.setOnClickListener {
                viewModel.onPersonalDataClicked()
            }

            textViewNotificationSettings.setOnClickListener {
                viewModel.onNotificationsClicked()
            }

            textViewContactUs.setOnClickListener {
                viewModel.onContactUsClicked()
            }

            textViewPrivacyPolicy.setOnClickListener {
                viewModel.onPrivacyPolicyClicked()
            }

            textViewLogOut.setOnClickListener {
                viewModel.onLogOutClicked()
            }
        }//Listeners

        // Observers
        viewModel.apply {
            onCollect(settingsEvent) { event ->
                when (event) {
                    SettingsViewModel.SettingsEvent.NavigateToActivityManagerFragment -> {
                        val action =
                            SettingsFragmentDirections.actionSettingsFragmentToActivityManagerFragment()
                        findNavController().navigate(action)
                    }//NavigateToActivityManagerFragment

                    SettingsViewModel.SettingsEvent.OpenExternalLinkOfPrivacyPolicy -> {
                        startPrivacyPolicyActivity()
                    }//OpenExternalLinkOfPrivacyPolicy

                    SettingsViewModel.SettingsEvent.NavigateToContactUsFragment -> {
                        val action =
                            SettingsFragmentDirections.actionSettingsFragmentToContactUsFragment()
                        findNavController().navigate(action)
                    }//NavigateToContactUsFragment

                    SettingsViewModel.SettingsEvent.NavigateToPersonalDataFragment -> {
                        val action =
                            SettingsFragmentDirections.actionSettingsFragmentToPersonalDataFragment()
                        findNavController().navigate(action)
                    }//NavigateToPersonalDataFragment

                    SettingsViewModel.SettingsEvent.NavigateToLogOutDialogFragment -> {
                        val action =
                            SettingsFragmentDirections.actionSettingsFragmentToLogOutDialogFragment()
                        findNavController().navigate(action)
                    }//NavigateToLogOutDialogFragment

                    SettingsViewModel.SettingsEvent.NavigateToNotificationsSettingsFragment -> {
                        val action =
                            SettingsFragmentDirections.actionSettingsFragmentToNotificationsSettingsFragment()
                        findNavController().navigate(action)
                    }//NavigateToNotificationsSettingsFragment
                }
            }
        }//Observers
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun startPrivacyPolicyActivity() {
        val url = "https://www.termsfeed.com/live/f82740ca-2d0a-4ad6-b488-5149f19f142d"
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}