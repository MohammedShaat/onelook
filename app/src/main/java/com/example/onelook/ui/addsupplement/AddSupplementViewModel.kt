package com.example.onelook.ui.addsupplement

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.R
import com.example.onelook.util.adapters.SelectableRectWithText
import com.example.onelook.util.adapters.SelectableOvalNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSupplementViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val state: SavedStateHandle
) : ViewModel() {

    val formsList = listOf(
        SelectableRectWithText(context.getString(R.string.pill), R.drawable.ic_supplement_pill),
        SelectableRectWithText(context.getString(R.string.tablet), R.drawable.ic_supplement_tablet),
        SelectableRectWithText(context.getString(R.string.sachet), R.drawable.ic_supplement_sachet),
        SelectableRectWithText(context.getString(R.string.drops), R.drawable.ic_supplement_drop),
        SelectableRectWithText(context.getString(R.string.spoon), R.drawable.ic_supplement_spoon),
    )
    val dosagesList = listOf(
        SelectableOvalNumber("1"),
        SelectableOvalNumber("2"),
        SelectableOvalNumber("3"),
        SelectableOvalNumber("4"),
        SelectableOvalNumber("5"),
        SelectableOvalNumber("6"),
    )
    val frequenciesList =
        listOf(SpinnerItem(1, context.getString(R.string.frequency_every_day))) +
                (2..30).map { n ->
                    SpinnerItem(n.toLong(), context.getString(R.string.frequency_every_n_days, n))
                }
    val durationsList =
        listOf(
            SpinnerItem(0, context.getString(R.string.duration_un_specified)),
            SpinnerItem(1, context.getString(R.string.duration_one_day))
        ) + (2..30).map { n ->
            SpinnerItem(n.toLong(), context.getString(R.string.duration_n_days, n))
        }
    val timesOfDayList = listOf(
        SelectableRectWithText(context.getString(R.string.morning), R.drawable.ic_morning),
        SelectableRectWithText(context.getString(R.string.afternoon), R.drawable.ic_afternoon),
        SelectableRectWithText(context.getString(R.string.evening), R.drawable.ic_evening),
        SelectableRectWithText(context.getString(R.string.night), R.drawable.ic_night),
    )
    val takingWithMealsList = listOf(
        context.getString(R.string.before_meal),
        context.getString(R.string.after_meal),
        context.getString(R.string.during_meal),
    )

    private val _name = state.getLiveData("selected_name", "")
    val name: LiveData<String>
        get() = _name

    private val _selectedForm = state.getLiveData("selected_form", -1)
    val selectedForm: LiveData<Int>
        get() = _selectedForm

    private val _selectedDosage = state.getLiveData("selected_dosage", -1)
    val selectedDosage: LiveData<Int>
        get() = _selectedDosage

    private val _selectedFrequency = state.getLiveData("selected_frequency", -1)
    val selectedFrequency: LiveData<Int>
        get() = _selectedFrequency

    private val _selectedDuration = state.getLiveData("selected_duration", -1)
    val selectedDuration: LiveData<Int>
        get() = _selectedDuration

    private val _selectedTimeOfDay = state.getLiveData("selected_time_of_day", -1)
    val selectedTimeOfDay: LiveData<Int>
        get() = _selectedTimeOfDay

    private val _customTime = state.getLiveData<String?>("custom_time", null)
    val customTime: LiveData<String?>
        get() = _customTime

    private val _selectedTakingWithMeals = state.getLiveData("selected_taking_with_meals", -1)
    val selectedTakingWithMeals: LiveData<Int>
        get() = _selectedTakingWithMeals


    private val _addSupplementEvent = MutableSharedFlow<AddSupplementEvent>()
    val addSupplementEvent = _addSupplementEvent.asSharedFlow()

    fun onButtonCloseClicked() = viewModelScope.launch {
        _addSupplementEvent.emit(AddSupplementEvent.CloseDialog)
    }

    fun onNameChanged(newName: String) {
        _name.value = newName
    }

    fun onFormSelected(newPosition: Int) {
        _selectedForm.value = newPosition
    }

    fun onDosageSelected(newPosition: Int) {
        _selectedDosage.value = newPosition
        if (dosagesList.getOrNull(newPosition)?.number?.toInt() != 1) {
            _selectedTimeOfDay.value = -1
            _customTime.value = null
        }
    }

    fun onFrequencySelected(newPosition: Int) {
        _selectedFrequency.value = newPosition
    }

    fun onDurationSelected(newPosition: Int) {
        _selectedDuration.value = newPosition
    }

    fun onTimeOfDaySelected(newPosition: Int) = viewModelScope.launch {
        if (dosagesList.getOrNull(_selectedDosage.value!!)?.number?.toInt() != 1) {
            _addSupplementEvent.emit(AddSupplementEvent.ShowCannotAddCustomTimeMessage)
            return@launch
        }
        _selectedTimeOfDay.value = newPosition
        _customTime.value = null
    }

    fun onButtonAddCustomTimeClicked() = viewModelScope.launch {
        if (dosagesList.getOrNull(_selectedDosage.value!!)?.number?.toInt() != 1)
            _addSupplementEvent.emit(AddSupplementEvent.ShowCannotAddCustomTimeMessage)
        else
            _addSupplementEvent.emit(AddSupplementEvent.ShowTimePicker)
    }

    fun onCustomTimeAdded(newCustomTime: String?) {
        _customTime.value = newCustomTime
        _selectedTimeOfDay.value = -1
    }

    sealed class AddSupplementEvent {
        object CloseDialog : AddSupplementEvent()
        object ShowTimePicker : AddSupplementEvent()
        object ShowCannotAddCustomTimeMessage : AddSupplementEvent()
    }
}