package com.example.onelook.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentHomeBinding
import com.example.onelook.ui.mainactivity.MainActivity
import com.example.onelook.util.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.net.UnknownHostException

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Shows bottom navigation
        showBottomNavigation()
        bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation)

        // Binding
        binding = FragmentHomeBinding.bind(view)
        setupToolbar()

        // Shows the first name of the user in greeting
        binding.textViewGreeting.text =
            getString(R.string.home_text_view_greeting, viewModel.userFirstName)

        // Sets up today tasks recyclerView
        val todayTasksAdapter = TodayTasksAdapter(
            resources,
            viewModel::onSupplementHistoryClicked,
            viewModel::onActivityHistoryClicked
        )
        binding.recyclerViewTodayTasks.apply {
            setHasFixedSize(true)
            adapter = todayTasksAdapter
        }

        // Sets up swipe refresh layout and set listener
        setupAndHandleSwipeRefreshLayout()

        // Listeners
        binding.apply {

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onSwipeRefreshSwiped()
            }

            imageButtonAddTask.setOnClickListener {
                viewModel.onAddEventClicked()
            }

            textViewAddTask.setOnClickListener {
                viewModel.onAddEventClicked()
            }
        }//Listeners

        // Observers
        viewModel.apply {
            // Today tasks
            onCollect(todayTasks) { result ->
                todayTasksAdapter.submitList(result.data)
                binding.apply {
//                    recyclerViewTodayTasks.scrollToPosition(0)
                    imageViewNotData.isVisible =
                        result.data.isNullOrEmpty() && result is CustomResult.Success
                }
            }

            // ProgressBar of loading
            onCollect(isLoading) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }

            // ProgressBar of refreshing
            onCollect(isRefreshing) { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }

            // Events
            onCollect(homeEvent) { event ->
                when (event) {
                    is HomeViewModel.HomeEvent.ShowRefreshFailedMessage -> {
                        val msg = when (event.exception) {
                            is UnknownHostException -> R.string.you_are_offline
                            else -> R.string.unexpected_error
                        }
                        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                            .setAnchorView(bottomNavigation)
                            .show()
                    }//ShowRefreshFailedMessage

                    is HomeViewModel.HomeEvent.NavigateToAddTaskDialog -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToAddTaskDialog()
                        findNavController().navigate(action)
                    }//NavigateToAddTaskDialog

                    is HomeViewModel.HomeEvent.NavigateToSupplementHistoryDetailsFragment -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToSupplementHistoryDetails(
                                event.supplementHistory
                            )
                        findNavController().navigate(action)
                    }//NavigateToSupplementHistoryDetailsFragment

                    is HomeViewModel.HomeEvent.NavigateToTimerFragment -> {
//                        val action =
//                            HomeFragmentDirections.actionGlobalTimerFragment(event.activityHistory)
//                        findNavController().navigate(action)
                        (requireActivity() as MainActivity).selectBottomNavigationSettingsItem(event.activityHistory)
                    }//NavigateToTimerFragment
                }
            }//homeEvent

            // Shows supplement created successfully snackBar
            setFragmentResultListener(ADD_SUPPLEMENT_REQ_KEY) { _, bundle ->
                val supplementName = bundle.getString(SUPPLEMENT_NAME_KEY)?.capital
                Snackbar.make(
                    view,
                    getString(R.string.supplement_added, supplementName),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(bottomNavigation)
                    .show()
            }

            // Shows activity created successfully snackBar
            setFragmentResultListener(ADD_ACTIVITY_REQ_KEY) { _, bundle ->
                val activityName = bundle.getString(ACTIVITY_TYPE_KEY)?.capital
                Snackbar.make(
                    view,
                    getString(R.string.activity_added, activityName),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(bottomNavigation)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).hideSplashScreen()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.menu_fragment_home)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_notifications -> {
                        // For testing
                        getNotificationManager()?.sendNotification(
                            context,
                            "testing notification",
                            ACTIVITIES_TIMER_CHANNEL_ID
                        )
                        true
                    }
                    else -> false
                }
            }
        }
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