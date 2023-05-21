package com.example.onelook.ui.login

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentLoginBinding
import com.example.onelook.util.onCollect

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLoginBinding.bind(view)

        // Listeners
        binding.apply {
            sendInputsDataWhenChanged()

            buttonLogin.setOnClickListener {
                viewModel.onButtonLoginClicked()
            }

            imageButtonPasswordVisibility.setOnClickListener {
                viewModel.onPasswordVisibilityClicked()
            }

            textViewForgotYourPassword.setOnClickListener {
                viewModel.onForgetPasswordClicked()
            }

            textViewSignUp.setOnClickListener {
                viewModel.onSignUpClicked()
            }
        }//Listeners

        // Observers
        viewModel.apply {
            passwordVisibility.observe(viewLifecycleOwner) { isPasswordVisible ->
                if (isPasswordVisible) {
                    binding.apply {
                        textInputPassword.transformationMethod = null
                        imageButtonPasswordVisibility.setImageResource(R.drawable.ic_password_visible)
                    }

                } else {
                    binding.apply {
                        textInputPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        imageButtonPasswordVisibility.setImageResource(R.drawable.ic_password_invisible)
                    }
                }
            }

            onCollect(loginEvent) { message ->
                when (message) {
                    is LoginViewModel.LoginEvent.EmptyFields -> {
                        markErrorFields(message.fields)
                        binding.textViewPasswordErrorMessage.apply {
                            setText(R.string.empty_fields)
                            isVisible = true
                        }
                    }//EmptyFields

                    is LoginViewModel.LoginEvent.HideErrors -> {
                        hideErrors()
                    }//HideErrors

                    is LoginViewModel.LoginEvent.NavigateToPasswordReminder1Fragment -> {
                        val action = LoginFragmentDirections.actionLoginFragmentToPasswordReminder1Fragment()
                        findNavController().navigate(action)
                    }//NavigateToPasswordReminder1Fragment

                    is LoginViewModel.LoginEvent.NavigateToSignUpFragment -> {
                        findNavController().navigate(R.id.signUpFragment)
                    }//NavigateToSignUpFragment
                }//when

            }
        }//Observers
    }//onViewCreated

    // Marks fields' labels that have error
    private fun markErrorFields(fields: List<LoginViewModel.LoginEvent.Fields>) {
        fields.forEach { field ->
            when (field) {
                LoginViewModel.LoginEvent.Fields.EMAIL -> binding.textViewEmail.setTextColor(
                    resources.getColor(R.color.alert)
                )
                LoginViewModel.LoginEvent.Fields.PASSWORD -> binding.textViewPassword.setTextColor(
                    resources.getColor(R.color.alert)
                )
            }
        }
    }

    // Sends fields' data when the user types
    private fun sendInputsDataWhenChanged() {
        binding.apply {
            textInputEmail.addTextChangedListener { viewModel.email.value = it.toString() }
            textInputPassword.addTextChangedListener { viewModel.password.value = it.toString() }
        }
    }

    // Resets labels and hide error message
    private fun hideErrors() {
        binding.apply {
            textViewEmail.setTextColor(resources.getColor(R.color.dark_grey))
            textViewPassword.setTextColor(resources.getColor(R.color.dark_grey))
            textViewPasswordErrorMessage.isVisible = false
        }
    }
}