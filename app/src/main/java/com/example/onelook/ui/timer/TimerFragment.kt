package com.example.onelook.ui.timer

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.onelook.R
import com.example.onelook.databinding.FragmentTimerBinding
import com.example.onelook.ui.home.activityIcon
import com.example.onelook.util.capital
import com.example.onelook.util.onCollect
import com.example.onelook.util.showBottomNavigation
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
        _binding = FragmentTimerBinding.bind(view)

        // Sets up the icon, text, and timer
        binding.apply {
            val activityHistory = viewModel.activityHistory.value
            if (activityHistory != null) {
                imageViewActivityIcon.activityIcon(activityHistory.formattedType)
                textViewActivityType.text = activityHistory.type.capital
            }
        }

        // Listeners
        binding.apply {

            imageButtonPlay.setOnClickListener {
                viewModel.onButtonPlayClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(isPlaying) { isPlaying ->
                if (isPlaying) {
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_play)
                }
            }

            onCollect(timer) { time ->
                binding.textViewTimer.text = time
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

    override fun onStop() {
        super.onStop()
        viewModel.onFragmentStop()
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