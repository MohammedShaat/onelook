package com.example.onelook.notifications.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentNotificationsBinding
import com.example.onelook.tasks.doamin.model.ActivityHistory
import com.example.onelook.notifications.doamin.model.Notification
import com.example.onelook.tasks.doamin.model.SupplementHistory
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.hideBottomNavigation
import com.example.onelook.common.util.mainActivity
import com.example.onelook.common.util.onCollect
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val viewModel: NotificationsViewModel by viewModels()
    private var _binding: FragmentNotificationsBinding? = null
    private val binding: FragmentNotificationsBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        _binding = FragmentNotificationsBinding.bind(view)

        // Populates activities list recyclerView
        val notificationAdapter = NotificationAdapter(viewModel::onNotificationClicked)
        binding.recyclerViewNotificationsList.apply {
            setHasFixedSize(true)
            adapter = notificationAdapter
        }


        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(notifications) { result ->
                notificationAdapter.submitList(result.data)
                binding.apply {
                    textViewNoData.isVisible =
                        result.data.isNullOrEmpty() && result !is Resource.Loading
                }
            }

            onCollect(isLoading) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }

            onCollect(notificationsEvent) { event ->
                when (event) {
                    is NotificationsViewModel.NotificationsEvent.OpenTask -> {
                        openActivityOrSupplement(event.notification)
                    }//OpenTask

                    NotificationsViewModel.NotificationsEvent.ShowThereIsActivityRunningMessage -> {
                        Toast.makeText(
                            context,
                            R.string.there_is_activity_running,
                            Toast.LENGTH_SHORT
                        ).show()
                    }//ShowThereIsActivityRunningMessage
                }
            }
        }//Observers
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openActivityOrSupplement(notification: Notification) {
        if (notification.historyType == SupplementHistory::class.java.name) {
            Timber.i("notification supplement clicked")
            val supplementHistory = notification.history as SupplementHistory
            val action = NotificationsFragmentDirections.actionGlobalSupplementHistoryDetails(
                supplementHistory
            )
            findNavController().navigate(action)
        } else {
            Timber.i("notification activity clicked")
            val activityHistory = notification.history as ActivityHistory
            mainActivity.selectBottomNavigationSettingsItem(activityHistory)
        }
    }

}