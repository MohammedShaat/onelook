package com.example.onelook.profile_and_settings.presentation.personaldata

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
class PersonalDataViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val name: String
        get() = profileRepository.getName()!!
    val email: String?
        get() = profileRepository.getEmail()

    private val _personalDataEvent = MutableSharedFlow<PersonalDataEvent>()
    val personalDataEvent = _personalDataEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onEditNameClicked() = viewModelScope.launch {
        _personalDataEvent.emit(PersonalDataEvent.NavigateToChangeNameFragment)
    }

    fun onChangePasswordClicked() = viewModelScope.launch {
        email ?: return@launch
        _isLoading.value = true
        val result = profileRepository.changePassword()
        if (result is Resource.Success)
            _personalDataEvent.emit(PersonalDataEvent.ShowPasswordRestEmailSentMessage)
        else
            _personalDataEvent.emit(PersonalDataEvent.ShowNameChangingFailedMessage(result.exception))
        _isLoading.value = false
    }

    sealed class PersonalDataEvent {
        object NavigateToChangeNameFragment : PersonalDataEvent()
        object ShowPasswordRestEmailSentMessage : PersonalDataEvent()
        data class ShowNameChangingFailedMessage(val exception: Exception?) : PersonalDataEvent()
    }
}