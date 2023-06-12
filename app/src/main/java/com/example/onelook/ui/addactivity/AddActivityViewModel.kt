package com.example.onelook.ui.addactivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.R
import com.example.onelook.util.adapters.SelectableOvalWithText
import com.example.onelook.util.adapters.SelectableRectWithText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val state: SavedStateHandle
) : ViewModel() {

    val typesList = listOf(
        SelectableOvalWithText(context.getString(R.string.running), R.drawable.ic_activity_running),
        SelectableOvalWithText(context.getString(R.string.walking), R.drawable.ic_activity_walking),
        SelectableOvalWithText(context.getString(R.string.fitness), R.drawable.ic_activity_fitness),
        SelectableOvalWithText(context.getString(R.string.yoga), R.drawable.ic_activity_yoga),
        SelectableOvalWithText(
            context.getString(R.string.rollerskating),
            R.drawable.ic_activity_rollers
        ),
        SelectableOvalWithText(
            context.getString(R.string.breathing),
            R.drawable.ic_activity_breath
        ),
    )
    val timesOfDayList = listOf(
        SelectableRectWithText(context.getString(R.string.morning), R.drawable.ic_morning),
        SelectableRectWithText(context.getString(R.string.afternoon), R.drawable.ic_afternoon),
        SelectableRectWithText(context.getString(R.string.evening), R.drawable.ic_evening),
        SelectableRectWithText(context.getString(R.string.night), R.drawable.ic_night),
    )

    private val _selectedType = state.getLiveData("selected_type", -1)
    val selectedType: LiveData<Int>
        get() = _selectedType

    private val _selectedTimeOfDay = state.getLiveData("selected_time_of_day", -1)
    val selectedTimeOfDay: LiveData<Int>
        get() = _selectedTimeOfDay

    private val _customTime = state.getLiveData<String?>("custom_time", null)
    val customTime: LiveData<String?>
        get() = _customTime

    private val _hourDuration = state.getLiveData("hour_duration", 0)
    val hourDuration: LiveData<Int>
        get() = _hourDuration

    private val _minuteDuration = state.getLiveData("minute_duration", 0)
    val minuteDuration: LiveData<Int>
        get() = _minuteDuration

    private val _addActivityEvent = MutableSharedFlow<AddActivityEvent>()
    val addActivityEvent = _addActivityEvent.asSharedFlow()

    fun onButtonCloseClicked() = viewModelScope.launch {
        _addActivityEvent.emit(AddActivityEvent.CloseDialog)
    }

    fun onTypeSelected(newPosition: Int) {
        _selectedType.value = newPosition
    }

    fun onTimeOfDaySelected(newPosition: Int) = viewModelScope.launch {
        _selectedTimeOfDay.value = newPosition
        _customTime.value = null
    }

    fun onButtonAddCustomTimeClicked() = viewModelScope.launch {
        _addActivityEvent.emit(AddActivityEvent.ShowTimePicker)
    }

    fun onCustomTimeAdded(newCustomTime: String?) {
        _customTime.value = newCustomTime
        _selectedTimeOfDay.value = -1
    }

    fun onNumberPickerHoursChanged(newHour: Int) {
        _hourDuration.value = newHour
    }

    fun onNumberPickerMinutesChanged(newMinute: Int) {
        _minuteDuration.value = newMinute
    }

    sealed class AddActivityEvent {
        object CloseDialog : AddActivityEvent()
        object ShowTimePicker : AddActivityEvent()
        object ShowCannotAddCustomTimeMessage : AddActivityEvent()
    }
}