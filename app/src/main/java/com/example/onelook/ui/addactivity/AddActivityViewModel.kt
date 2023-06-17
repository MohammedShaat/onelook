package com.example.onelook.ui.addactivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.R
import com.example.onelook.data.Repository
import com.example.onelook.data.local.activities.LocalActivity
import com.example.onelook.data.local.supplements.LocalSupplement
import com.example.onelook.util.CustomResult
import com.example.onelook.util.OperationSource
import com.example.onelook.util.adapters.SelectableOvalWithText
import com.example.onelook.util.adapters.SelectableRectWithText
import com.example.onelook.util.toTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    @ApplicationContext context: Context,
    state: SavedStateHandle,
    private val repository: Repository
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

    private val _reminderBefore = state.getLiveData<Boolean>("reminder_before", false)
    private val _reminderAfter = state.getLiveData<Boolean>("reminder_after", false)

    private val _addActivityEvent = MutableSharedFlow<AddActivityEvent>()
    val addActivityEvent = _addActivityEvent.asSharedFlow()

    private var _errorFields = emptyList<Fields>()
    val errorFields: List<Fields>
        get() = _errorFields

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

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

    fun onSwitchReminderBeforeSwitched(isChecked: Boolean) {
        _reminderBefore.value = isChecked
    }

    fun onSwitchReminderAfterSwitched(isChecked: Boolean) {
        _reminderAfter.value = isChecked
    }

    fun onButtonAddSupplementClicked() = viewModelScope.launch {
        _isLoading.emit(true)
        _errorFields = getEmptyRequiredFields()
        if (_errorFields.isNotEmpty()) {
            _isLoading.emit(false)
            _addActivityEvent.emit(AddActivityEvent.ShowFillRequiredFieldsMessage)
            return@launch
        }

        createActivity().collect { result ->
            if (result is CustomResult.Success) {
                _addActivityEvent.emit(
                    AddActivityEvent.NavigateBackAfterSupplementAdded(typesList[_selectedType.value!!].text)
                )
                _isLoading.emit(false)
            }
        }
    }

    private fun createActivity(): Flow<CustomResult<OperationSource>> {
        val creationDateTime = SimpleDateFormat(
            "y-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Calendar.getInstance().time)

        val localActivity = LocalActivity(
            id = UUID.randomUUID(),
            type = typesList[_selectedType.value!!].text.lowercase(),
            duration = "${_hourDuration.value!!.toTimeString()}:${_minuteDuration.value!!.toTimeString()}",
            timeOfDay = _customTime.value?.replace(" ", "")
                ?: timesOfDayList[_selectedTimeOfDay.value!!].text.lowercase(),
            reminder = when {
                _reminderBefore.value!! && _reminderAfter.value!! -> "both"
                _reminderBefore.value!! -> "before"
                else -> "after"
            },
            createdAt = creationDateTime,
            updatedAt = creationDateTime,
        )
        Timber.i(localActivity.toString())
        return repository.createActivity(localActivity, creationDateTime)
    }

    private fun getEmptyRequiredFields(): List<Fields> {
        val fields = mutableListOf<Fields>()
        if (_selectedType.value == -1) fields.add(Fields.TYPE)
        if (_hourDuration.value == 0 && _minuteDuration.value == 0) fields.add(Fields.DURATION)
        if (_selectedTimeOfDay.value == -1 && _customTime.value == null) fields.add(Fields.TIME_OF_DAY)
        return fields
    }

    sealed class AddActivityEvent {
        object CloseDialog : AddActivityEvent()
        object ShowTimePicker : AddActivityEvent()
        object ShowCannotAddCustomTimeMessage : AddActivityEvent()
        object ShowFillRequiredFieldsMessage : AddActivityEvent()
        data class NavigateBackAfterSupplementAdded(val activityType: String) : AddActivityEvent()
    }

    enum class Fields {
        TYPE, TIME_OF_DAY, DURATION
    }
}