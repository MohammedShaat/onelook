package com.example.onelook.profile_and_settings.presentation.contactus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ContactUsViewModel : ViewModel() {

    private val _contactUsEvent = MutableSharedFlow<ContactUsEvent>()
    val contactUsEvent = _contactUsEvent.asSharedFlow()
    fun onPhoneNumberClicked() = viewModelScope.launch {
        _contactUsEvent.emit(ContactUsEvent.DialPhoneNumber)
    }

    fun onPhoneNumberClickedLong() = viewModelScope.launch {
        _contactUsEvent.emit(ContactUsEvent.CopyPhoneNumber)
    }

    fun onEmailClicked() = viewModelScope.launch {
        _contactUsEvent.emit(ContactUsEvent.SendEmail)
    }

    fun onEmailClickedLong() = viewModelScope.launch {
        _contactUsEvent.emit(ContactUsEvent.CopyEmail)
    }

    fun onLocationClicked() = viewModelScope.launch {
        _contactUsEvent.emit(ContactUsEvent.DisplayLocation)
    }

    fun onLocationClickedLong() = viewModelScope.launch {
        _contactUsEvent.emit(ContactUsEvent.CopyLocation)
    }

    sealed class ContactUsEvent {
        object DialPhoneNumber : ContactUsEvent()
        object CopyPhoneNumber : ContactUsEvent()
        object SendEmail : ContactUsEvent()
        object CopyEmail : ContactUsEvent()
        object DisplayLocation : ContactUsEvent()
        object CopyLocation : ContactUsEvent()
    }
}