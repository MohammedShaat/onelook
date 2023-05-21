package com.example.onelook.ui.signup

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentSignUpBinding
import com.example.onelook.util.onCollect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private lateinit var binding: FragmentSignUpBinding
    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup
        binding = FragmentSignUpBinding.bind(view)
        navController = findNavController()

        viewModel.onSignUpVisited()

        // Listeners
        binding.apply {
            sendInputsDataWhenChanged()

            buttonSignUp.setOnClickListener {
                viewModel.onButtonSignUpClicked()
            }

            imageButtonPasswordVisibility.setOnClickListener {
                viewModel.onPasswordVisibilityClicked()
            }

            textViewLogin.setOnClickListener {
                viewModel.onLoginClicked()
            }
        }//Listeners

        // Observers
        viewModel.apply {
            buttonSignUpEnabled.observe(viewLifecycleOwner) { isEnabled ->
                binding.buttonSignUp.isEnabled = isEnabled
            }

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

            onCollect(singUpEvent) { message ->
                when (message) {
                    is SignUpViewModel.SignUpEvent.EmptyFields -> {
                        markErrorFields(message.fields)
                        binding.textViewPasswordErrorMessage.apply {
                            setText(R.string.empty_fields)
                            isVisible = true
                        }
                    }//EmptyFields event

                    is SignUpViewModel.SignUpEvent.HideErrors -> {
                        hideErrors()
                    }//HideErrors event

                    is SignUpViewModel.SignUpEvent.NavigateToLoginFragment -> {
                        navController.popBackStack()
                        navController.navigate(R.id.loginFragment)
                    }
                }//when
            }
        }//Observers
    }//onViewCreated

    // Marks fields' labels that have error
    private fun markErrorFields(fields: List<SignUpViewModel.SignUpEvent.Fields>) {
        fields.forEach { field ->
            when (field) {
                SignUpViewModel.SignUpEvent.Fields.NAME -> binding.textViewName.setTextColor(
                    resources.getColor(R.color.alert)
                )
                SignUpViewModel.SignUpEvent.Fields.EMAIL -> binding.textViewEmail.setTextColor(
                    resources.getColor(R.color.alert)
                )
                SignUpViewModel.SignUpEvent.Fields.PASSWORD -> binding.textViewPassword.setTextColor(
                    resources.getColor(R.color.alert)
                )

            }
        }
    }

    // Sends fields' data when the user types
    private fun sendInputsDataWhenChanged() {
        binding.apply {
            textInputName.addTextChangedListener { viewModel.name.value = it.toString() }
            textInputEmail.addTextChangedListener { viewModel.email.value = it.toString() }
            textInputPassword.addTextChangedListener { viewModel.password.value = it.toString() }
            checkboxPrivacyPolicy.setOnCheckedChangeListener { _, isChecked ->
                viewModel.buttonSignUpEnabled.value = isChecked
            }
        }
    }

    // Resets labels and hide error message
    private fun hideErrors() {
        binding.apply {
            textViewName.setTextColor(resources.getColor(R.color.dark_grey))
            textViewEmail.setTextColor(resources.getColor(R.color.dark_grey))
            textViewPassword.setTextColor(resources.getColor(R.color.dark_grey))
            textViewPasswordErrorMessage.isVisible = false
        }
    }
}