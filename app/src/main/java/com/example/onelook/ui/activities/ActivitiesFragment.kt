package com.example.onelook.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.GLOBAL_TAG
import com.example.onelook.R
import com.example.onelook.databinding.FragmentActivitiesBinding
import com.example.onelook.ui.supplements.SupplementsFragmentDirections
import com.example.onelook.util.Constants
import com.example.onelook.util.Constants.UPDATE_ACTIVITY_REQ_KEY
import com.example.onelook.util.CustomResult
import com.example.onelook.util.onCollect
import com.example.onelook.util.toDomainModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.net.UnknownHostException

@AndroidEntryPoint
class ActivitiesFragment : Fragment(R.layout.fragment_activities) {

    private val viewModel: ActivitiesViewModel by viewModels()
    private var _binding: FragmentActivitiesBinding? = null
    private val binding: FragmentActivitiesBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentActivitiesBinding.bind(view)

        // Populates activities list recyclerView
        val activitiesAdapter = ActivityAdapter { activity ->
            viewModel.onEditActivityClicked(activity)
        }
        binding.recyclerViewActivitiesList.apply {
            setHasFixedSize(true)
            adapter = activitiesAdapter
        }

        // Sets up swipe refresh layout and set listener
        setupAndHandleSwipeRefreshLayout()

        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onSwipeRefreshSwiped()
            }

            buttonAddActivity.setOnClickListener {
                viewModel.onButtonAddActivityClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            // Activities list
            onCollect(activities) { result ->
                activitiesAdapter.submitList(result.data)
                binding.imageViewNotData.isVisible =
                    result.data.isNullOrEmpty() && result is CustomResult.Success
            }

            // Loading indicator
            onCollect(isLoading) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }

            // Refreshing indicator
            onCollect(isRefreshing) { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }

            // Events
            onCollect(activitiesEvent) { event ->
                when (event) {
                    ActivitiesViewModel.ActivitiesEvent.NavigateToAddActivityFragment -> {
                        val action =
                            ActivitiesFragmentDirections.actionGlobalAddEditActivityFragment(null)
                        findNavController().navigate(action)
                    }//NavigateToAddActivityFragment

                    is ActivitiesViewModel.ActivitiesEvent.ShowRefreshFailedMessage -> {
                        val msg = when (event.exception) {
                            is UnknownHostException -> R.string.you_are_offline
                            else -> R.string.unexpected_error
                        }
                        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.buttonAddActivity)
                            .show()
                    }//ShowRefreshFailedMessage

                    is ActivitiesViewModel.ActivitiesEvent.NavigateToAddEditActivityFragmentForEditing -> {
                        val action =
                            SupplementsFragmentDirections.actionGlobalAddEditActivityFragment(
                                event.activity
                            )
                        findNavController().navigate(action)
                    }//NavigateToAddEditActivityFragmentForEditing
                }
            }
        }//Observers

        // Shows activity updated successfully snackBar
        setFragmentResultListener(UPDATE_ACTIVITY_REQ_KEY) { _, bundle ->
            val supplementName = bundle.getString(Constants.SUPPLEMENT_NAME_KEY)
            Snackbar.make(
                view,
                getString(R.string.activity_updated, supplementName),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(binding.buttonAddActivity)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupAndHandleSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(
                ResourcesCompat.getColor(resources, R.color.turquoise, null),
                ResourcesCompat.getColor(resources, R.color.purple_plum, null),
            )
        }
    }
}