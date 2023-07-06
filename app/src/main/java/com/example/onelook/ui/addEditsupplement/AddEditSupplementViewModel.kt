package com.example.onelook.ui.addEditsupplement

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.R
import com.example.onelook.data.Repository
import com.example.onelook.data.domain.Supplement
import com.example.onelook.data.local.supplements.LocalSupplement
import com.example.onelook.util.CustomResult
import com.example.onelook.util.adapters.SelectableOvalNumber
import com.example.onelook.util.adapters.SelectableRectWithText
import com.example.onelook.util.capital
import com.example.onelook.util.format
import com.example.onelook.util.isExpired
import com.example.onelook.util.parseDate
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
class AddEditSupplementViewModel @Inject constructor(
    @ApplicationContext context: Context,
    state: SavedStateHandle,
    private val repository: Repository
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

    private val _supplement = state.getLiveData<Supplement?>("supplement", null)
    val supplement: LiveData<Supplement?>
        get() = _supplement

    init {
        Timber.i("supplement:: ${supplement.value}")
    }

    val updateSupplement = _supplement.value != null

    private val _name = state.getLiveData("selected_name", _supplement.value?.name ?: "")
    val name: LiveData<String>
        get() = _name

    private val _selectedForm = state.getLiveData(
        "selected_form",
        formsList.indexOfFirst { it.text == _supplement.value?.form })
    val selectedForm: LiveData<Int>
        get() = _selectedForm

    private val _selectedDosage = state.getLiveData(
        "selected_dosage",
        dosagesList.indexOfFirst { it.number.toInt() == _supplement.value?.dosage })
    val selectedDosage: LiveData<Int>
        get() = _selectedDosage

    private val _selectedFrequency = state.getLiveData(
        "selected_frequency",
        frequenciesList.indexOfFirst { it.text == _supplement.value?.frequency })
    val selectedFrequency: LiveData<Int>
        get() = _selectedFrequency

    private val _selectedDuration = state.getLiveData(
        "selected_duration",
        durationsList.indexOfFirst { it.text == _supplement.value?.duration })
    val selectedDuration: LiveData<Int>
        get() = _selectedDuration

    private val _selectedTimeOfDay = state.getLiveData(
        "selected_time_of_day",
        timesOfDayList.indexOfFirst { it.text == _supplement.value?.timeOfDay })
    val selectedTimeOfDay: LiveData<Int>
        get() = _selectedTimeOfDay

    private val _customTime = state.getLiveData<String?>(
        "custom_time",
        _supplement.value?.timeOfDay?.takeIf { it.contains(":") })
    val customTime: LiveData<String?>
        get() = _customTime

    private val _selectedTakingWithMeals = state.getLiveData("selected_taking_with_meals",
        takingWithMealsList.indexOfFirst { it == _supplement.value?.takingWithMeals })
    val selectedTakingWithMeals: LiveData<Int>
        get() = _selectedTakingWithMeals

    private val _reminderBefore = state.getLiveData(
        "reminder_before", _supplement.value?.reminder in listOf("before", "both")
    )
    val reminderBefore: LiveData<Boolean>
        get() = _reminderBefore

    private val _reminderAfter = state.getLiveData(
        "reminder_after",
        _supplement.value?.reminder in listOf("after", "both")
    )
    val reminderAfter: LiveData<Boolean>
        get() = _reminderAfter

    private val _addSupplementEvent = MutableSharedFlow<AddSupplementEvent>()
    val addSupplementEvent = _addSupplementEvent.asSharedFlow()

    private var _errorFields = emptyList<Fields>()
    val errorFields: List<Fields>
        get() = _errorFields

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


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

    fun onChipTakingWithMealsClicked(newPosition: Int?) {
        _selectedTakingWithMeals.value = newPosition ?: -1
    }

    fun onSwitchReminderBeforeSwitched(isChecked: Boolean) {
        _reminderBefore.value = isChecked
    }

    fun onSwitchReminderAfterSwitched(isChecked: Boolean) {
        _reminderAfter.value = isChecked
    }

    fun onButtonAddEditSupplementClicked() = viewModelScope.launch {
        _isLoading.emit(true)
        _errorFields = getEmptyRequiredFields()
        if (_errorFields.isNotEmpty()) {
            _isLoading.emit(false)
            _addSupplementEvent.emit(AddSupplementEvent.ShowFillRequiredFieldsMessage)
            return@launch
        }

        createOrUpdateSupplement()
    }

    private fun createOrUpdateSupplement() = viewModelScope.launch {
        val formattedDate = Date().format
        val isExpired = isExpired(
            _supplement.value?.createdAt?.parseDate ?: Date(),
            durationsList[_selectedDuration.value!!].text.takeIf { _selectedDuration.value != 0 }
        )

        val localSupplement = LocalSupplement(
            id = _supplement.value?.id ?: UUID.randomUUID(),
            name = _name.value!!,
            form = formsList[_selectedForm.value!!].text,
            dosage = dosagesList[_selectedDosage.value!!].number.toInt(),
            frequency = frequenciesList[_selectedFrequency.value!!].text,
            duration = durationsList[_selectedDuration.value!!].text.takeIf { it != durationsList[0].text },
            timeOfDay = timesOfDayList.getOrNull(_selectedTimeOfDay.value!!)?.text
                ?: _customTime.value?.replace(" ", ""),
            takingWithMeals = takingWithMealsList[_selectedTakingWithMeals.value!!],
            reminder = when {
                _reminderBefore.value!! && _reminderAfter.value!! -> "both"
                _reminderBefore.value!! -> "before"
                _reminderAfter.value!! -> "after"
                else -> null
            },
            completed = isExpired,
            createdAt = _supplement.value?.createdAt ?: formattedDate,
            updatedAt = formattedDate,
        )
        Timber.i("local supplement ready")

        if (_supplement.value == null)
            repository.createSupplement(localSupplement).collect { result ->
                if (result is CustomResult.Success) {
                    _addSupplementEvent.emit(
                        AddSupplementEvent.NavigateBackAfterSupplementAdded(localSupplement.name.capital)
                    )
                    _isLoading.emit(false)
                }
            }
        else
            repository.updateSupplement(localSupplement).collect { result ->
                if (result is CustomResult.Success) {
                    _addSupplementEvent.emit(
                        AddSupplementEvent.NavigateBackAfterSupplementUpdated(localSupplement.name.capital)
                    )
                    _isLoading.emit(false)
                }
            }
    }

    private fun getEmptyRequiredFields(): List<Fields> {
        val fields = mutableListOf<Fields>()
        if (_name.value!!.isBlank()) fields.add(Fields.NAME)
        if (_selectedForm.value == -1) fields.add(Fields.FORM)
        if (_selectedDosage.value == -1) fields.add(Fields.DOSAGE)
        if (dosagesList.getOrNull(_selectedDosage.value!!)?.number?.toInt() == 1 &&
            (_selectedTimeOfDay.value == -1 && _customTime.value == null)
        )
            fields.add(Fields.TIME_OF_DAY)
        if (_selectedTakingWithMeals.value == -1) fields.add(Fields.TAKING_WITH_MEALS)
        return fields
    }

    sealed class AddSupplementEvent {
        object CloseDialog : AddSupplementEvent()
        object ShowTimePicker : AddSupplementEvent()
        object ShowCannotAddCustomTimeMessage : AddSupplementEvent()
        object ShowFillRequiredFieldsMessage : AddSupplementEvent()
        data class NavigateBackAfterSupplementAdded(val supplementName: String) :
            AddSupplementEvent()

        data class NavigateBackAfterSupplementUpdated(val supplementName: String) :
            AddSupplementEvent()
    }

    enum class Fields {
        NAME, FORM, DOSAGE, TIME_OF_DAY, TAKING_WITH_MEALS
    }
}