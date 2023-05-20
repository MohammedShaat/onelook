package com.example.onelook.ui.signup

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.onelook.R
import com.example.onelook.databinding.FragmentSignUpBinding
import com.example.onelook.util.onCollect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private lateinit var binding: FragmentSignUpBinding
    private val viewModel: SignUpViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Sets up binding
        binding = FragmentSignUpBinding.bind(view)

        viewModel.onSignUpVisited()

        // Listeners
        binding.apply {
            sendInputsDataWhenChanged()

            buttonSignUp.setOnClickListener {
                viewModel.onButtonSignUpClicked()
            }

            checkboxPrivacyPolicy.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onCheckBoxPrivacyPolicyChanged()
            }

            imageButtonPasswordVisibility.setOnClickListener {
                viewModel.onPasswordVisibilityClicked()
            }
        }

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

            onCollect(hideErrorsEvent) {
                hideErrors()
            }

            onCollect(inputsErrorMessageEvent) { message ->
                val textViewPasswordErrorMessage = binding.textViewPasswordErrorMessage
                when (message) {
                    is SignUpViewModel.InputsErrorMessage.EmptyFields -> {
                        textViewPasswordErrorMessage.setText(R.string.empty_fields)
                    }//EmptyFields
                }//when
                markErrorFields(message.fields)
                textViewPasswordErrorMessage.isVisible = true
            }
        }//Listeners
    }//onViewCreated

    // Marks fields' labels that have error
    private fun markErrorFields(fields: List<SignUpViewModel.InputsErrorMessage.Fields>) {
        fields.forEach { field ->
            when (field) {
                SignUpViewModel.InputsErrorMessage.Fields.NAME -> binding.textViewName.setTextColor(
                    resources.getColor(R.color.alert)
                )
                SignUpViewModel.InputsErrorMessage.Fields.EMAIL -> binding.textViewEmail.setTextColor(
                    resources.getColor(R.color.alert)
                )
                SignUpViewModel.InputsErrorMessage.Fields.PASSWORD -> binding.textViewPassword.setTextColor(
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