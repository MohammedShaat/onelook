package com.example.onelook.ui.addsupplement

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
import com.example.onelook.databinding.ChipTakingWithMealsBinding
import com.example.onelook.databinding.FragmentAddSupplementBinding
import com.example.onelook.util.Constants.ADD_SUPPLEMENT_REQ_KEY
import com.example.onelook.util.Constants.SUPPLEMENT_NAME_KEY
import com.example.onelook.util.adapters.SelectableItemAdapter
import com.example.onelook.util.hideBottomNavigation
import com.example.onelook.util.onCollect
import com.example.onelook.util.toTimeString
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class AddSupplementFragment : Fragment(R.layout.fragment_add_supplement) {

    private val viewModel: AddSupplementViewModel by viewModels()
    private lateinit var binding: FragmentAddSupplementBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()

        // Binding
        binding = FragmentAddSupplementBinding.bind(view)

        // Populates forms RecyclerView
        val formsAdapter =
            SelectableItemAdapter(viewModel.formsList, viewModel.selectedForm.value!!) { position ->
                viewModel.onFormSelected(position)
            }
        binding.recyclerViewForms.apply {
            setHasFixedSize(true)
            adapter = formsAdapter
        }

        // Populates dosages RecyclerView
        val dosageAdapter =
            SelectableItemAdapter(
                viewModel.dosagesList,
                viewModel.selectedDosage.value!!
            ) { position ->
                viewModel.onDosageSelected(position)
            }
        binding.recyclerViewDosages.apply {
            setHasFixedSize(true)
            adapter = dosageAdapter
        }

        // Populates frequency Spinner
        val frequencyAdapter = CustomSpinnerAdapter(viewModel.frequenciesList) { position ->
            viewModel.onFrequencySelected(position)
        }
        binding.spinnerFrequency.apply {
            adapter = frequencyAdapter
        }

        // Populates duration Spinner
        val durationAdapter = CustomSpinnerAdapter(viewModel.durationsList) { position ->
            viewModel.onDurationSelected(position)
        }
        binding.spinnerDuration.apply {
            adapter = durationAdapter
        }

        // Populates times of day RecyclerView
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

        // Populates taking with meals chips
        binding.chipGroupTakingWithMeals.apply {
            val inflater = LayoutInflater.from(this.context)
            val chips = viewModel.takingWithMealsList.mapIndexed { idx, time ->
                ChipTakingWithMealsBinding.inflate(inflater, this, false).root.apply {
                    id = idx
                    text = time
                    isChecked = viewModel.selectedTakingWithMeals.value == idx
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

            textInputName.addTextChangedListener {
                viewModel.onNameChanged(it.toString())
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

            switchReminderBefore.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSwitchReminderAfterSwitched(isChecked)
            }

            buttonAddSupplement.setOnClickListener {
                hideErrorFields()
                viewModel.onButtonAddSupplementClicked()
            }

        }//Listeners


        // Observers
        viewModel.apply {

            selectedForm.observe(viewLifecycleOwner) { position ->
                formsAdapter.updateSelectedItem(position)
            }

            selectedDosage.observe(viewLifecycleOwner) { position ->
                dosageAdapter.updateSelectedItem(position)
            }

            selectedTimeOfDay.observe(viewLifecycleOwner) { position ->
                timesOfDayAdapter.updateSelectedItem(position)
            }

            customTime.observe(viewLifecycleOwner) { time ->
                binding.buttonAddCustomTime.text =
                    time ?: getString(R.string.button_add_custom_time)
            }

            onCollect(isLoading) { isLoading ->
                binding.apply {
                    buttonAddSupplement.isEnabled = !isLoading
                    progressBar.isVisible = isLoading
                }
            }

            onCollect(addSupplementEvent) { event ->
                when (event) {
                    is AddSupplementViewModel.AddSupplementEvent.CloseDialog -> {
                        findNavController().popBackStack()
                    }//CloseDialog

                    is AddSupplementViewModel.AddSupplementEvent.ShowTimePicker -> {
                        showTimePicker()
                    }//ShowTimePicker

                    is AddSupplementViewModel.AddSupplementEvent.ShowCannotAddCustomTimeMessage -> {
                        Snackbar.make(view, R.string.dosage_must_be_one, Snackbar.LENGTH_SHORT)
                            .show()
                    }//ShowCannotAddCustomTimeMessage

                    is AddSupplementViewModel.AddSupplementEvent.ShowFillRequiredFieldsMessage -> {
                        markErrorFields(viewModel.errorFields)
                        Snackbar.make(view, R.string.fill_required_fields, Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.buttonAddSupplement)
                            .show()
                    }//ShowFillRequiredFieldsMessage

                    is AddSupplementViewModel.AddSupplementEvent.NavigateBackAfterSupplementAdded -> {
                        setFragmentResult(ADD_SUPPLEMENT_REQ_KEY, Bundle().apply {
                            putString(SUPPLEMENT_NAME_KEY, event.supplementName)
                        })
                        findNavController().popBackStack()
                    }//SupplementCreationSucceeded
                }
            }
        }//Observers
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

    private fun markErrorFields(fields: List<AddSupplementViewModel.Fields>) {
        val color = ContextCompat.getColor(requireContext(), R.color.alert)
        fields.forEach { field ->
            when (field) {
                AddSupplementViewModel.Fields.NAME -> binding.textViewName.setTextColor(color)
                AddSupplementViewModel.Fields.FORM -> binding.textViewForm.setTextColor(color)
                AddSupplementViewModel.Fields.DOSAGE -> binding.textViewDosage.setTextColor(color)
                AddSupplementViewModel.Fields.TIME_OF_DAY ->
                    binding.textViewTimeOfDay.setTextColor(color)
                AddSupplementViewModel.Fields.TAKING_WITH_MEALS ->
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