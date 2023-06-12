package com.example.onelook.ui.addsupplement

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.ChipTakingWithMealsBinding
import com.example.onelook.databinding.FragmentAddSupplementBinding
import com.example.onelook.util.adapters.SelectableItemAdapter
import com.example.onelook.util.hideBottomNavigation
import com.example.onelook.util.onCollect
import com.example.onelook.util.showToast
import com.example.onelook.util.toTimeString
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
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
            SelectableItemAdapter(viewModel.dosagesList, viewModel.selectedDosage.value!!) { position ->
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
            val chips = viewModel.takingWithMealsList.map { time ->
                ChipTakingWithMealsBinding.inflate(inflater, this, false).root.apply {
                    text = time
                    isChecked = false
                }
            }

            chips.forEach(::addView)
            setOnCheckedStateChangeListener { group, checkedIds ->
                Timber.i("Chip: $checkedIds")
            }
        }


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

            onCollect(addSupplementEvent) { event ->
                when (event) {
                    is AddSupplementViewModel.AddSupplementEvent.CloseDialog -> {
                        findNavController().popBackStack()
                    }//CloseDialog

                    is AddSupplementViewModel.AddSupplementEvent.ShowTimePicker -> {
                        showTimePicker()
                    }//ShowTimePicker

                    is AddSupplementViewModel.AddSupplementEvent.ShowCannotAddCustomTimeMessage -> {
                        showToast(R.string.dosage_must_be_one)
                    }//ShowCannotAddCustomTimeMessage
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
}