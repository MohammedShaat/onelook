package com.example.onelook.ui.addactivity

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentAddActivityBinding
import com.example.onelook.ui.addsupplement.AddSupplementViewModel
import com.example.onelook.util.adapters.SelectableItemAdapter
import com.example.onelook.util.hideBottomNavigation
import com.example.onelook.util.onCollect
import com.example.onelook.util.showToast
import com.example.onelook.util.toTimeString
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddActivityFragment : Fragment(R.layout.fragment_add_activity) {

    private val viewModel: AddActivityViewModel by viewModels()
    private lateinit var binding: FragmentAddActivityBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()

        // Binding
        binding = FragmentAddActivityBinding.bind(view)

        // Populates types RecyclerView
        val typesAdapter =
            SelectableItemAdapter(viewModel.typesList, viewModel.selectedType.value!!) { position ->
                viewModel.onTypeSelected(position)
            }
        binding.recyclerViewTypes.apply {
            setHasFixedSize(true)
            adapter = typesAdapter
        }

        // Populates time of day RecyclerView
        val timesOfDayAdapter = SelectableItemAdapter(
            viewModel.timesOfDayList,
            viewModel.selectedTimeOfDay.value!!
        ) { position ->
            viewModel.onTimeOfDaySelected(position)
        }
        binding.recyclerViewTimeOfDay.apply {
            setHasFixedSize(true)
            adapter = timesOfDayAdapter
        }

        // Sets up duration number picker
        binding.apply {
            val formatter = NumberPicker.Formatter { num ->
                num.toTimeString()
            }
            numberPickerHours.apply {
                setFormatter(formatter)
                minValue = 0
                maxValue = 24
                value = viewModel.hourDuration.value!!
            }
            numberPickerMinutes.apply {
                setFormatter(formatter)
                minValue = 0
                maxValue = 60
                value = viewModel.minuteDuration.value!!
            }
        }


        // Listeners
        binding.apply {

            imageButtonClose.setOnClickListener {
                viewModel.onButtonCloseClicked()
            }

            buttonAddCustomTime.setOnClickListener {
                viewModel.onButtonAddCustomTimeClicked()
            }

            numberPickerHours.setOnValueChangedListener { _, _, newVal ->
                viewModel.onNumberPickerHoursChanged(newVal)
            }

            numberPickerMinutes.setOnValueChangedListener { _, _, newVal ->
                viewModel.onNumberPickerMinutesChanged(newVal)
            }
        }//Listeners


        // Observers
        viewModel.apply {

            selectedType.observe(viewLifecycleOwner) { position ->
                typesAdapter.updateSelectedItem(position)
            }

            selectedTimeOfDay.observe(viewLifecycleOwner) { position ->
                timesOfDayAdapter.updateSelectedItem(position)
            }

            customTime.observe(viewLifecycleOwner) { time ->
                binding.buttonAddCustomTime.text =
                    time ?: getString(R.string.button_add_custom_time)
            }

            onCollect(addActivityEvent) { event ->
                when (event) {
                    is AddActivityViewModel.AddActivityEvent.CloseDialog -> {
                        findNavController().popBackStack()
                    }//CloseDialog

                    is AddActivityViewModel.AddActivityEvent.ShowTimePicker -> {
                        showTimePicker()
                    }//ShowTimePicker

                    is AddActivityViewModel.AddActivityEvent.ShowCannotAddCustomTimeMessage -> {
                        showToast(R.string.dosage_must_be_one)
                    }//ShowCannotAddCustomTimeMessage
                }
            }
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                if (hourOfDay == 0 && minute == 0) return@OnTimeSetListener
                viewModel.onCustomTimeAdded(
                    getString(
                        R.string.custom_time_text,
                        hourOfDay.toTimeString(),
                        minute.toTimeString()
                    )
                )
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
            .show()
    }
}