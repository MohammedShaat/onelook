package com.example.onelook.tasks.presentation.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddTaskViewModel : ViewModel() {

    private val _addTaskEvent = MutableSharedFlow<AddTaskEvent>()
    val addTaskEvent = _addTaskEvent.asSharedFlow()

    fun onButtonActivityClicked() = viewModelScope.launch {
        _addTaskEvent.emit(AddTaskEvent.NavigateToAddActivityDialog)
    }

    fun onButtonSupplementClicked() = viewModelScope.launch {
        _addTaskEvent.emit(AddTaskEvent.NavigateToAddSupplementDialog)
    }

    sealed class AddTaskEvent {
        object NavigateToAddActivityDialog : AddTaskEvent()
        object NavigateToAddSupplementDialog : AddTaskEvent()
    }
}