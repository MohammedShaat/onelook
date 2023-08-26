package com.example.onelook.tasks.presentation.add_edit_supplement

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.common.util.ADD_SUPPLEMENT_REQ_KEY
import com.example.onelook.common.util.REMINDER_TIME_ADDITION
import com.example.onelook.common.util.SUPPLEMENT_NAME_KEY
import com.example.onelook.common.util.UPDATE_SUPPLEMENT_REQ_KEY
import com.example.onelook.common.util.adapters.SelectableItemAdapter
import com.example.onelook.common.util.capital
import com.example.onelook.common.util.hideBottomNavigation
import com.example.onelook.common.util.onCollect
import com.example.onelook.common.util.to24Format
import com.example.onelook.databinding.ChipTakingWithMealsBinding
import com.example.onelook.databinding.FragmentAddEditSupplementBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class AddEditSupplementFragment : Fragment(R.layout.fragment_add_edit_supplement) {

    private val viewModel: AddEditSupplementViewModel by viewModels()
    private lateinit var binding: FragmentAddEditSupplementBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()

        // Binding
        binding = FragmentAddEditSupplementBinding.bind(view)

        // Sets up views
        binding.apply {
            textViewHeader.setText(
                if (viewModel.updateSupplement) R.string.text_view_header_update
                else R.string.add_supplement_text_view_header
            )
            buttonAddEditSupplement.setText(
                if (viewModel.updateSupplement) R.string.button_confirm_changes
                else R.string.button_add_supplement
            )
            buttonCancel.isVisible = viewModel.updateSupplement

            switchReminderBefore.text =
                getString(R.string.text_view_reminder_before, REMINDER_TIME_ADDITION)
            switchReminderAfter.text =
                getString(R.string.text_view_reminder_after, REMINDER_TIME_ADDITION)
        }

        // Populates forms RecyclerView
        val formsAdapter =
            SelectableItemAdapter(requireContext(), viewModel.formsList) { newPosition ->
                viewModel.onFormSelected(newPosition)
            }
        binding.recyclerViewForms.apply {
            setHasFixedSize(true)
            adapter = formsAdapter
        }

        // Populates dosages RecyclerView
        val dosageAdapter =
            SelectableItemAdapter(requireContext(), viewModel.dosagesList) { newPosition ->
                viewModel.onDosageSelected(newPosition)
            }
        binding.recyclerViewDosages.apply {
            setHasFixedSize(true)
            adapter = dosageAdapter
        }

        // Populates frequency Spinner
        val frequencyAdapter = CustomSpinnerAdapter(viewModel.frequenciesList) { newPosition ->
            viewModel.onFrequencySelected(newPosition)
        }
        binding.spinnerFrequency.apply {
            adapter = frequencyAdapter
        }

        // Populates duration Spinner
        val durationAdapter = CustomSpinnerAdapter(viewModel.durationsList) { newPosition ->
            viewModel.onDurationSelected(newPosition)
        }
        binding.spinnerDuration.apply {
            adapter = durationAdapter
        }

        // Populates times of day RecyclerView
        val timesOfDayAdapter =
            SelectableItemAdapter(requireContext(), viewModel.timesOfDayList) { newPosition ->
                viewModel.onTimeOfDaySelected(newPosition)
            }
        binding.recyclerViewTimeOfDay.apply {
            setHasFixedSize(true)
            adapter = timesOfDayAdapter
        }

        // Populates taking with meals chips
        binding.chipGroupTakingWithMeals.apply {
            val inflater = LayoutInflater.from(this.context)
            val chips = viewModel.takingWithMealsList.mapIndexed { idx, time ->
                ChipTakingWithMealsBinding.inflate(inflater, this, false).root.apply {
                    id = idx
                    text = getString(R.string.meal, time.capital)
                    isChecked = false
                }
            }

            removeAllViews()
            chips.forEach(::addView)
        }

        markErrorFields(viewModel.errorFields)


        // Listeners
        binding.apply {

            imageButtonClose.setOnClickListener {
                viewModel.onButtonCloseClicked()
            }

            textInputName.addTextChangedListener { newEditable ->
                viewModel.onNameChanged(newEditable.toString())
            }

            buttonAddCustomTime.setOnClickListener {
                viewModel.onButtonAddCustomTimeClicked()
            }

            chipGroupTakingWithMeals.setOnCheckedStateChangeListener { _, checkedIds ->
                viewModel.onChipTakingWithMealsClicked(checkedIds.firstOrNull())
            }

            switchReminderBefore.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSwitchReminderBeforeSwitched(isChecked)
            }

            switchReminderAfter.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSwitchReminderAfterSwitched(isChecked)
            }

            buttonAddEditSupplement.setOnClickListener {
                hideErrorFields()
                viewModel.onButtonAddEditSupplementClicked()
            }

            buttonCancel.setOnClickListener {
                viewModel.onButtonCloseClicked()
            }

        }//Listeners


        // Observers
        viewModel.apply {

            name.observe(viewLifecycleOwner) { newName ->
                binding.textInputName.apply {
                    if (newName == text.toString()) return@observe
                    setText(newName)
                    setSelection(newName.length)
                }
            }

            selectedForm.observe(viewLifecycleOwner) { newPosition ->
                formsAdapter.updateSelectedItem(newPosition)
            }

            selectedDosage.observe(viewLifecycleOwner) { newPosition ->
                dosageAdapter.updateSelectedItem(newPosition)
            }

            selectedFrequency.observe(viewLifecycleOwner) { newPosition ->
                binding.spinnerFrequency.setSelection(newPosition)
            }

            selectedDuration.observe(viewLifecycleOwner) { newPosition ->
                binding.spinnerDuration.setSelection(newPosition)
            }

            selectedTimeOfDay.observe(viewLifecycleOwner) { newPosition ->
                timesOfDayAdapter.updateSelectedItem(newPosition)
            }

            customTime.observe(viewLifecycleOwner) { time ->
                binding.buttonAddCustomTime.text =
                    time ?: getString(R.string.button_add_custom_time)
            }

            selectedTakingWithMeals.observe(viewLifecycleOwner) { newPosition ->
                if (newPosition == -1) return@observe
                binding.chipGroupTakingWithMeals.check(newPosition)
            }

            reminderBefore.observe(viewLifecycleOwner) { isChecked ->
                binding.switchReminderBefore.isChecked = isChecked
            }

            reminderAfter.observe(viewLifecycleOwner) { isChecked ->
                binding.switchReminderAfter.isChecked = isChecked
            }

            onCollect(isLoading) { isLoading ->
                binding.apply {
                    buttonAddEditSupplement.isEnabled = !isLoading
                    buttonCancel.isEnabled = !isLoading
                    progressBar.isVisible = isLoading
                }
            }

            onCollect(addSupplementEvent) { event ->
                when (event) {
                    is AddEditSupplementViewModel.AddSupplementEvent.CloseDialog -> {
                        findNavController().popBackStack()
                    }//CloseDialog

                    is AddEditSupplementViewModel.AddSupplementEvent.ShowTimePicker -> {
                        showTimePicker()
                    }//ShowTimePicker

                    is AddEditSupplementViewModel.AddSupplementEvent.ShowCannotAddCustomTimeMessage -> {
                        Snackbar.make(view, R.string.dosage_must_be_one, Snackbar.LENGTH_SHORT)
                            .show()
                    }//ShowCannotAddCustomTimeMessage

                    is AddEditSupplementViewModel.AddSupplementEvent.ShowFillRequiredFieldsMessage -> {
                        markErrorFields(viewModel.errorFields)
                        Snackbar.make(view, R.string.fill_required_fields, Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.buttonAddEditSupplement)
                            .show()
                    }//ShowFillRequiredFieldsMessage

                    is AddEditSupplementViewModel.AddSupplementEvent.NavigateBackAfterSupplementAdded -> {
                        setFragmentResult(ADD_SUPPLEMENT_REQ_KEY, Bundle().apply {
                            putString(SUPPLEMENT_NAME_KEY, event.supplementName)
                        })
                        findNavController().popBackStack()
                    }//NavigateBackAfterSupplementAdded

                    is AddEditSupplementViewModel.AddSupplementEvent.NavigateBackAfterSupplementUpdated -> {
                        setFragmentResult(UPDATE_SUPPLEMENT_REQ_KEY, Bundle().apply {
                            putString(SUPPLEMENT_NAME_KEY, event.supplementName)
                        })
                        findNavController().popBackStack()
                    }//NavigateBackAfterSupplementUpdated
                }
            }
        }//Observers
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
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

    private fun markErrorFields(fields: List<AddEditSupplementViewModel.Fields>) {
        val color = ContextCompat.getColor(requireContext(), R.color.alert)
        fields.forEach { field ->
            when (field) {
                AddEditSupplementViewModel.Fields.NAME -> binding.textViewName.setTextColor(color)
                AddEditSupplementViewModel.Fields.FORM -> binding.textViewForm.setTextColor(color)
                AddEditSupplementViewModel.Fields.DOSAGE -> binding.textViewDosage.setTextColor(
                    color
                )

                AddEditSupplementViewModel.Fields.TIME_OF_DAY ->
                    binding.textViewTimeOfDay.setTextColor(color)

                AddEditSupplementViewModel.Fields.TAKING_WITH_MEALS ->
                    binding.textViewTakingWithMeals.setTextColor(color)
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
            textViewName.setTextColor(color)
            textViewForm.setTextColor(color)
            textViewDosage.setTextColor(color)
            textViewTimeOfDay.setTextColor(color)
            textViewTakingWithMeals.setTextColor(color)
        }
    }
}