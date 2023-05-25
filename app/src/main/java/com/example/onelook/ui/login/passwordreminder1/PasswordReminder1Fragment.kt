package com.example.onelook.ui.login.passwordreminder1

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentPasswordReminder1Binding
import com.example.onelook.ui.login.LoginViewModel
import com.example.onelook.util.Constants.PASSWORD_REST_EMAIL_REQ
import com.example.onelook.util.onCollect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordReminder1Fragment : Fragment(R.layout.fragment_password_reminder_1) {

    private lateinit var binding: FragmentPasswordReminder1Binding
    private val viewModel: PasswordReminder1ViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPasswordReminder1Binding.bind(view)

        // Listeners
        binding.apply {
            textInputEmail.addTextChangedListener {
                viewModel.email.value = it.toString()
            }

            buttonConfirmEmail.setOnClickListener {
                viewModel.onButtonConfirmEmailClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(isLoading) { isLoading ->
                binding.apply {
                    progressBar.isVisible = isLoading
                    buttonConfirmEmail.isEnabled = !isLoading
                }
            }

            onCollect(passwordReminder1Event) { event ->
                hideError()
                when (event) {
                    is PasswordReminder1ViewModel.PasswordReminder1Event.ShowEmptyEmailFieldMessage -> {
                        markErrorEmailField()
                        binding.textViewMessage.apply {
                            setText(R.string.empty_email_field)
                            isVisible = true
                        }
                    }//ShowEmptyEmailFieldMessage

                    is PasswordReminder1ViewModel.PasswordReminder1Event.ShowSendPasswordResetEmailFailedMessage -> {
                        markErrorEmailField()
                        val textViewMessage = binding.textViewMessage
                        textViewMessage.isVisible = true
                        when (event.exception) {
                            PasswordReminder1ViewModel.SendPasswordResetEmailExceptions.INVALID_EMAIL -> {
                                textViewMessage.setText(R.string.not_exist_email_reset_password)
                            }
                            PasswordReminder1ViewModel.SendPasswordResetEmailExceptions.NETWORK_ISSUE -> {
                                textViewMessage.setText(R.string.no_internet_connection)
                            }
                            PasswordReminder1ViewModel.SendPasswordResetEmailExceptions.OTHER_EXCEPTIONS -> {
                                textViewMessage.text =
                                    event.message ?: getString(R.string.unexpected_error_2)
                            }
                        }
                    }

                    is PasswordReminder1ViewModel.PasswordReminder1Event.NavigateBackToLoginFragment -> {
                        setFragmentResult(PASSWORD_REST_EMAIL_REQ, Bundle().apply {
                            putString("email", email.value!!)
                        })
                        findNavController().popBackStack()
                    }
                }
            }//passwordReminder1Event
        }//Observers
    }

    private fun hideError() {
        binding.apply {
            textViewEmail.setTextColor(resources.getColor(R.color.dark_grey))
            textViewMessage.isVisible = false
        }
    }

    private fun markErrorEmailField() {
        binding.textViewEmail.setTextColor(resources.getColor(R.color.alert))
    }
}