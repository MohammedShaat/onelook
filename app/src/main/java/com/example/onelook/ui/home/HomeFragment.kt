package com.example.onelook.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.ui.mainactivity.MainActivity
import com.example.onelook.R
import com.example.onelook.databinding.FragmentHomeBinding
import com.example.onelook.util.CustomResult
import com.example.onelook.util.onCollect
import com.example.onelook.util.showBottomNavigation
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.net.UnknownHostException

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Shows bottom navigation
        showBottomNavigation()

        // Binding
        binding = FragmentHomeBinding.bind(view)
        setupToolbar()

        // Shows the first name of the user in greeting
        binding.textViewGreeting.text =
            getString(R.string.home_text_view_greeting, viewModel.userFirstName)

        // Sets up RecyclerView
        val todayTasksAdapter = TodayTasksAdapter(resources)
        binding.recyclerViewTodayTasks.apply {
            setHasFixedSize(true)
            adapter = todayTasksAdapter
        }

        // Sets up swipe refresh layout and set listener
        setupAndHandleSwipeRefreshLayout()

        // Listeners
        binding.apply {
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
                            .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                            .show()
                    }//ShowRefreshFailedMessage

                    is HomeViewModel.HomeEvent.NavigateToAddTaskDialog -> {
                        val action = HomeFragmentDirections.actionGlobalAddTaskDialog()
                        findNavController().navigate(action)
                    }//NavigateToAddTaskDialog
                }
            }//homeEvent
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).hideSplashScreen()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.menu_fragment_home)
        }
    }

    private fun setupAndHandleSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(
                ResourcesCompat.getColor(resources, R.color.turquoise, null),
                ResourcesCompat.getColor(resources, R.color.purple_plum, null),
            )

            setOnRefreshListener {
                viewModel.onSwipeRefreshSwiped()
            }
        }
    }
}