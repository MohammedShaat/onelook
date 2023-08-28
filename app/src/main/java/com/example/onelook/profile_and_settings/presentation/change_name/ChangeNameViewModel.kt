package com.example.onelook.profile_and_settings.presentation.change_name

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.profile_and_settings.doamin.repository.ProfileRepository
import com.example.onelook.common.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ChangeNameViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _name = savedState.getLiveData("name", "")
    val name: LiveData<String>
        get() = _name

    private val _changeNameEvent = MutableSharedFlow<ChangeNameEvent>()
    val changeNameEvent = _changeNameEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isErrorVisible = savedState.getLiveData("isErrorVisible", false)
    val isErrorVisible: LiveData<Boolean>
        get() = _isErrorVisible

    fun onNameChanged(newName: String) {
        _name.value = newName
    }

    fun onCancelClicked() = viewModelScope.launch {
        _changeNameEvent.emit(ChangeNameEvent.NavigateBack)
    }

    fun onErrorVisibilityChanged(isVisible: Boolean) {
        if (isVisible != _isErrorVisible.value)
            _isErrorVisible.value = isVisible
    }

    fun onConfirmChangesClicked() = viewModelScope.launch {
        if (!nameIsValid()) {
            _changeNameEvent.emit(ChangeNameEvent.ShowEmptyNameFieldMessage)
            return@launch
        }

        _isLoading.value = true
        val result = profileRepository.changeName(_name.value!!)
        if (result is Resource.Success)
            _changeNameEvent.emit(ChangeNameEvent.NavigateBackAfterNameUpdated)
        else
            _changeNameEvent.emit(ChangeNameEvent.ShowNameChangingFailedMessage(result.exception))
        _isLoading.value = false

    }

    private fun nameIsValid(): Boolean {
        return name.value!!.isNotBlank()
    }

    sealed class ChangeNameEvent {
        object NavigateBack : ChangeNameEvent()
        object ShowEmptyNameFieldMessage : ChangeNameEvent()
        object NavigateBackAfterNameUpdated : ChangeNameEvent()
        data class ShowNameChangingFailedMessage(val exception: Exception?) : ChangeNameEvent()
    }
}