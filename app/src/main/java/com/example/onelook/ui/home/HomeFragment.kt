package com.example.onelook.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.onelook.ui.mainactivity.MainActivity
import com.example.onelook.R
import com.example.onelook.databinding.FragmentHomeBinding
import com.example.onelook.util.onCollect
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binding
        binding = FragmentHomeBinding.bind(view)
        // Shows bottom navigation
        (requireActivity() as MainActivity).showBottomNavigation()
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

        // Observers
        viewModel.apply {
            // Today tasks
            onCollect(todayTasks) { tasks ->
                todayTasksAdapter.submitList(tasks)
                Timber.i("tasks size: ${tasks.size}")
            }

            // ProgressBar of loading
            onCollect(isLoading) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }

            // ProgressBar of refreshing
            onCollect(isRefreshing) { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
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