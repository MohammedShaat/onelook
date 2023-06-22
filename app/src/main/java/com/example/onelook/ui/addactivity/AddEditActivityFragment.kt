package com.example.onelook.ui.addactivity

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentAddEditActivityBinding
import com.example.onelook.util.*
import com.example.onelook.util.adapters.SelectableItemAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddEditActivityFragment : Fragment(R.layout.fragment_add_edit_activity) {

    private val viewModel: AddEditActivityViewModel by viewModels()
    private lateinit var binding: FragmentAddEditActivityBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()

        // Binding
        binding = FragmentAddEditActivityBinding.bind(view)

        // Sets up the text of add/edit button and visibility of cancel button
        binding.apply {
            buttonAddEditActivity.setText(
                if (viewModel.updateActivity) R.string.button_confirm_changes
                else R.string.button_add_activity
            )
            buttonCancel.isVisible = viewModel.updateActivity
        }

        // Populates types RecyclerView
        val typesAdapter =
            SelectableItemAdapter(viewModel.typesList) { newPosition ->
                viewModel.onTypeSelected(newPosition)
            }
        binding.recyclerViewTypes.apply {
            setHasFixedSize(true)
            adapter = typesAdapter
        }

        // Populates time of day RecyclerView
        val timesOfDayAdapter = SelectableItemAdapter(viewModel.timesOfDayList) { newPosition ->
            viewModel.onTimeOfDaySelected(newPosition)
        }
        binding.recyclerViewTimeOfDay.apply {
            setHasFixedSize(true)
            adapter = timesOfDayAdapter
        }

        // Sets up duration number picker
        binding.apply {
            val formatter = NumberPicker.Formatter { num ->
                num.to24Format()
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

        markErrorFields(viewModel.errorFields)

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

            switchReminderBefore.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSwitchReminderBeforeSwitched(isChecked)
            }

            switchReminderAfter.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSwitchReminderAfterSwitched(isChecked)
            }

            buttonAddEditActivity.setOnClickListener {
                hideErrorFields()
                viewModel.onButtonAddEditActivityClicked()
            }

            buttonCancel.setOnClickListener {
                viewModel.onButtonCloseClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            selectedType.observe(viewLifecycleOwner) { newPosition ->
                typesAdapter.updateSelectedItem(newPosition)
            }

            selectedTimeOfDay.observe(viewLifecycleOwner) { newPosition ->
                timesOfDayAdapter.updateSelectedItem(newPosition)
            }

            hourDuration.observe(viewLifecycleOwner) { newHour ->
                binding.numberPickerHours.apply {
                    if (newHour == value) return@observe
                    value = newHour
                }
            }

            minuteDuration.observe(viewLifecycleOwner) { newMinute ->
                binding.numberPickerMinutes.apply {
                    if (newMinute == value) return@observe
                    value = newMinute
                }
            }

            customTime.observe(viewLifecycleOwner) { time ->
                binding.buttonAddCustomTime.text =
                    time ?: getString(R.string.button_add_custom_time)
            }

            reminderBefore.observe(viewLifecycleOwner) { isChecked ->
                binding.switchReminderBefore.isChecked = isChecked
            }

            reminderAfter.observe(viewLifecycleOwner) { isChecked ->
                binding.switchReminderAfter.isChecked = isChecked
            }

            onCollect(isLoading) { isLoading ->
                binding.apply {
                    buttonAddEditActivity.isEnabled = !isLoading
                    buttonCancel.isEnabled = !isLoading
                    progressBar.isVisible = isLoading
                }
            }

            onCollect(addEditActivityEvent) { event ->
                when (event) {
                    is AddEditActivityViewModel.AddEditActivityEvent.CloseDialog -> {
                        findNavController().popBackStack()
                    }//CloseDialog

                    is AddEditActivityViewModel.AddEditActivityEvent.ShowTimePicker -> {
                        showTimePicker()
                    }//ShowTimePicker

                    is AddEditActivityViewModel.AddEditActivityEvent.ShowCannotAddCustomTimeMessage -> {
                        Snackbar.make(view, R.string.dosage_must_be_one, Snackbar.LENGTH_SHORT)
                            .show()
                    }//ShowCannotAddCustomTimeMessage

                    is AddEditActivityViewModel.AddEditActivityEvent.ShowFillRequiredFieldsMessage -> {
                        markErrorFields(viewModel.errorFields)
                        Snackbar.make(view, R.string.fill_required_fields, Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.buttonAddEditActivity)
                            .show()
                    }//ShowFillRequiredFieldsMessage

                    is AddEditActivityViewModel.AddEditActivityEvent.NavigateBackAfterActivityAdded -> {
                        setFragmentResult(ADD_ACTIVITY_REQ_KEY, Bundle().apply {
                            putString(ACTIVITY_TYPE_KEY, event.activityType)
                        })
                        findNavController().popBackStack()
                    }//SupplementCreationSucceeded

                    is AddEditActivityViewModel.AddEditActivityEvent.NavigateBackAfterActivityUpdated -> {
                        setFragmentResult(UPDATE_ACTIVITY_REQ_KEY, Bundle().apply {
                            putString(SUPPLEMENT_NAME_KEY, event.activityType)
                        })
                        findNavController().popBackStack()
                    }//NavigateBackAfterSupplementUpdated
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
                        hourOfDay.to24Format(),
                        minute.to24Format()
                    )
                )
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
            .show()
    }

    private fun markErrorFields(fields: List<AddEditActivityViewModel.Fields>) {
        val color = ContextCompat.getColor(requireContext(), R.color.alert)
        fields.forEach { field ->
            when (field) {
                AddEditActivityViewModel.Fields.TYPE -> binding.textViewType.setTextColor(color)
                AddEditActivityViewModel.Fields.TIME_OF_DAY ->
                    binding.textViewTimeOfDay.setTextColor(color)
                AddEditActivityViewModel.Fields.DURATION -> binding.textViewDuration.setTextColor(
                    color
                )
            }
        }//fields
    }

    private fun hideErrorFields() {
        val typeArray = requireContext().theme.obtainStyledAttributes(
            intArrayOf(com.google.android.material.R.attr.colorOnSurface)
        )
        val color = typeArray.getColor(0, ContextCompat.getColor(requireContext(), R.color.black))
        typeArray.recycle()
        binding.apply {
            textViewType.setTextColor(color)
            textViewTimeOfDay.setTextColor(color)
            textViewDuration.setTextColor(color)
        }
    }
}