package com.example.onelook.tasks.presentation.add_edit_activity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.R
import com.example.onelook.tasks.data.local.ActivityEntity
import com.example.onelook.tasks.doamin.model.DomainActivity
import com.example.onelook.tasks.doamin.repository.ActivityRepository
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.adapters.SelectableOvalWithText
import com.example.onelook.common.util.adapters.SelectableRectWithText
import com.example.onelook.common.util.format
import com.example.onelook.common.util.to24Format
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditActivityViewModel @Inject constructor(
    @ApplicationContext context: Context,
    state: SavedStateHandle,
    private val activityRepository: ActivityRepository
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

    private val _activity = state.getLiveData<DomainActivity?>("activity", null)
    val activity: LiveData<DomainActivity?>
        get() = _activity

    init {
        Timber.i("activity:: ${activity.value}")
    }

    val updateActivity = _activity.value != null

    private val _selectedType = state.getLiveData(
        "selected_type",
        typesList.indexOfFirst { it.text == _activity.value?.type })
    val selectedType: LiveData<Int>
        get() = _selectedType

    private val _selectedTimeOfDay = state.getLiveData(
        "selected_time_of_day",
        timesOfDayList.indexOfFirst { it.text == _activity.value?.timeOfDay })
    val selectedTimeOfDay: LiveData<Int>
        get() = _selectedTimeOfDay

    private val _customTime = state.getLiveData<String?>(
        "custom_time",
        _activity.value?.timeOfDay?.takeIf { it.contains(":") })
    val customTime: LiveData<String?>
        get() = _customTime

    private val _hourDuration = state.getLiveData(
        "hour_duration",
        _activity.value?.duration?.substringBefore(":")?.toInt() ?: 0
    )
    val hourDuration: LiveData<Int>
        get() = _hourDuration

    private val _minuteDuration = state.getLiveData(
        "minute_duration",
        _activity.value?.duration?.substringAfter(":")?.toInt() ?: 0
    )
    val minuteDuration: LiveData<Int>
        get() = _minuteDuration

    private val _reminderBefore = state.getLiveData(
        "reminder_before", _activity.value?.reminder in listOf("before", "both")
    )
    val reminderBefore: LiveData<Boolean>
        get() = _reminderBefore

    private val _reminderAfter = state.getLiveData(
        "reminder_after",
        _activity.value?.reminder in listOf("after", "both")
    )
    val reminderAfter: LiveData<Boolean>
        get() = _reminderAfter

    private val _addEditActivityEvent = MutableSharedFlow<AddEditActivityEvent>()
    val addEditActivityEvent = _addEditActivityEvent.asSharedFlow()

    private var _errorFields = emptyList<Fields>()
    val errorFields: List<Fields>
        get() = _errorFields

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onButtonCloseClicked() = viewModelScope.launch {
        _addEditActivityEvent.emit(AddEditActivityEvent.CloseDialog)
    }

    fun onTypeSelected(newPosition: Int) {
        _selectedType.value = newPosition
    }

    fun onTimeOfDaySelected(newPosition: Int) = viewModelScope.launch {
        _selectedTimeOfDay.value = newPosition
        _customTime.value = null
    }

    fun onButtonAddCustomTimeClicked() = viewModelScope.launch {
        _addEditActivityEvent.emit(AddEditActivityEvent.ShowTimePicker)
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

    fun onButtonAddEditActivityClicked() = viewModelScope.launch {
        _isLoading.emit(true)
        _errorFields = getEmptyRequiredFields()
        if (_errorFields.isNotEmpty()) {
            _isLoading.emit(false)
            _addEditActivityEvent.emit(AddEditActivityEvent.ShowFillRequiredFieldsMessage)
            return@launch
        }

        createOrUpdateActivity()
    }

    private fun createOrUpdateActivity() = viewModelScope.launch {
        val formattedDate = Date().format

        val activityEntity = ActivityEntity(
            id = _activity.value?.id ?: UUID.randomUUID(),
            type = typesList[_selectedType.value!!].text.lowercase(),
            duration = "${_hourDuration.value!!.to24Format()}:${_minuteDuration.value!!.to24Format()}",
            timeOfDay = _customTime.value?.replace(" ", "")
                ?: timesOfDayList[_selectedTimeOfDay.value!!].text.lowercase(),
            reminder = when {
                _reminderBefore.value!! && _reminderAfter.value!! -> "both"
                _reminderBefore.value!! -> "before"
                _reminderAfter.value!! -> "after"
                else -> null
            },
            createdAt = _activity.value?.createdAt ?: formattedDate,
            updatedAt = formattedDate,
        )

        if (_activity.value == null)
            activityRepository.createActivity(activityEntity).collect { result ->
                if (result is Resource.Success) {
                    _addEditActivityEvent.emit(
                        AddEditActivityEvent.NavigateBackAfterActivityAdded(activityEntity.type)
                    )
                    _isLoading.emit(false)
                }
            }
        else
            activityRepository.updateActivity(activityEntity).collect { result ->
                if (result is Resource.Success) {
                    _addEditActivityEvent.emit(
                        AddEditActivityEvent.NavigateBackAfterActivityUpdated(activityEntity.type)
                    )
                    _isLoading.emit(false)
                }
            }
    }

    private fun getEmptyRequiredFields(): List<Fields> {
        val fields = mutableListOf<Fields>()
        if (_selectedType.value == -1) fields.add(Fields.TYPE)
        if (_hourDuration.value == 0 && _minuteDuration.value == 0) fields.add(Fields.DURATION)
        if (_selectedTimeOfDay.value == -1 && _customTime.value == null) fields.add(Fields.TIME_OF_DAY)
        return fields
    }

    sealed class AddEditActivityEvent {
        object CloseDialog : AddEditActivityEvent()
        object ShowTimePicker : AddEditActivityEvent()
        object ShowCannotAddCustomTimeMessage : AddEditActivityEvent()
        object ShowFillRequiredFieldsMessage : AddEditActivityEvent()
        data class NavigateBackAfterActivityAdded(val activityType: String) : AddEditActivityEvent()
        data class NavigateBackAfterActivityUpdated(val activityType: String) :
            AddEditActivityEvent()
    }

    enum class Fields {
        TYPE, TIME_OF_DAY, DURATION
    }
}