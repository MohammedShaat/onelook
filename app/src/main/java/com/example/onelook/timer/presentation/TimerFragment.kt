package com.example.onelook.timer.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.onelook.R
import com.example.onelook.databinding.FragmentTimerBinding
import com.example.onelook.tasks.presentation.home.activityIcon
import com.example.onelook.common.util.capital
import com.example.onelook.common.util.enableDoubleBackClick
import com.example.onelook.common.util.hideSplashScreen
import com.example.onelook.common.util.onCollect
import com.example.onelook.common.util.showBottomNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerFragment : Fragment(R.layout.fragment_timer) {

    private val viewModel: TimerViewModel by viewModels()
    private var _binding: FragmentTimerBinding? = null
    private val binding: FragmentTimerBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showBottomNavigation()
        enableDoubleBackClick()
        _binding = FragmentTimerBinding.bind(view)

        // Sets up the icon, text, and timer
        binding.apply {
            val activityHistory = viewModel.activityHistory
            if (activityHistory != null) {
                imageViewActivityIcon.activityIcon(activityHistory.parsedType)
                textViewActivityType.text = activityHistory.type.capital
            }
        }

        // Listeners
        binding.apply {

            imageButtonPlayPause.setOnClickListener {
                viewModel.onButtonPlayClicked()
            }

            imageButtonStop.setOnClickListener {
                viewModel.onButtonStopClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(isPlaying) { isPlaying ->
                if (isPlaying) {
                    binding.imageButtonPlayPause.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.imageButtonPlayPause.setImageResource(R.drawable.ic_play)
                }
            }

            onCollect(isStoppable) { isStoppable ->
                binding.imageButtonStop.isVisible = isStoppable
            }

            onCollect(isSaving) { isSaving ->
                binding.apply {
                    progressBar.isVisible = isSaving
                    imageButtonPlayPause.isEnabled = !isSaving
                    imageButtonStop.isEnabled = !isSaving
                }
            }

            onCollect(timer) { time ->
                binding.apply {
                    textViewTimer.text = time
                }
            }

            onCollect(timerEvent) { event ->
                when (event) {
                    TimerViewModel.TimerEvent.ShowActivityAlreadyFinishedMessage -> {
                        showToastOfActivityAlreadyFinished()
                    }//ShowActivityFinishedMessage
                }
            }
        }//Observers
    }

    override fun onResume() {
        super.onResume()
        hideSplashScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToastOfActivityAlreadyFinished() {
        Toast.makeText(
            context,
            R.string.activity_already_finished,
            Toast.LENGTH_SHORT
        ).show()
    }
}