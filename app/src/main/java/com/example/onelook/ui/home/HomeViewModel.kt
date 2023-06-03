package com.example.onelook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onelook.ui.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val auth: FirebaseAuth
) : ViewModel() {

    init {
        val user = auth.currentUser!!
        user.getIdToken(false).addOnCompleteListener { task ->
            if (task.isSuccessful) viewModelScope.launch {
                Timber.i("idToken: ${task.result.token}")
            } else viewModelScope.launch {
                Timber.i("getting token failed: ${task.exception}")
            }
        }
    }

}