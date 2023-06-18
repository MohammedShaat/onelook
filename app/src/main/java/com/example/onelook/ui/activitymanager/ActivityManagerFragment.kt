package com.example.onelook.ui.activitymanager

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentActivityManagerBinding
import com.example.onelook.util.hideBottomNavigation
import com.example.onelook.util.onCollect
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ActivityManagerFragment : Fragment(R.layout.fragment_activity_manager) {

    private val viewModel: ActivityManagerViewModel by viewModels()
    private var _binding: FragmentActivityManagerBinding? = null
    private val binding: FragmentActivityManagerBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentActivityManagerBinding.bind(view)
        hideBottomNavigation()

        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            textViewActivities.setOnClickListener {
                viewModel.onActivitiesClicked()
            }

            textViewSupplements.setOnClickListener {
                viewModel.onSupplementsClicked()
            }
        }

        // Observers
        viewModel.apply {

            onCollect(activityManagerEvent) { event ->
                when (event) {
                    ActivityManagerViewModel.ActivityManagerEvent.NavigateToActivitiesFragment -> {
                        val action =
                            ActivityManagerFragmentDirections.actionActivityManagerFragmentToActivitiesFragment()
                        findNavController().navigate(action)
                    }//NavigateToActivitiesFragment

                    ActivityManagerViewModel.ActivityManagerEvent.NavigateToSupplementsFragment -> {
                        val action =
                            ActivityManagerFragmentDirections.actionActivityManagerFragmentToSupplementsFragment()
                        findNavController().navigate(action)
                    }//NavigateToSupplementsFragment
                }
            }
        }//Observers
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}