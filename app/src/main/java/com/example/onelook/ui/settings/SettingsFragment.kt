package com.example.onelook.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentSettingsBinding
import com.example.onelook.ui.mainactivity.MainActivity
import com.example.onelook.util.onCollect
import com.example.onelook.util.showBottomNavigation
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSettingsBinding.bind(view)
        showBottomNavigation()

        // Listeners
        binding.apply {

            textViewActivityManager.setOnClickListener {
                viewModel.onActivityManagerClicked()
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
                    }
                }
            }
        }//Observers
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}