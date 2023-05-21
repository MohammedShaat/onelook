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
import com.google.android.material.snackbar.Snackbar
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
                viewModel.onButtonSignUpWithEmailClicked()
            }

            imageButtonPasswordVisibility.setOnClickListener {
                viewModel.onPasswordVisibilityClicked()
            }

            textViewLogin.setOnClickListener {
                viewModel.onLoginClicked()
            }

            imageButtonGoogle.setOnClickListener {

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

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.apply {
                    root.isClickable = !isLoading
                    buttonSignUp.isEnabled = !isLoading
                    progressBar.isVisible = isLoading
                }
            }

            onCollect(singUpEvent) { event ->
                when (event) {
                    is SignUpViewModel.SignUpEvent.EmptyFields -> {
                        markErrorFields(event.fields)
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

                    is SignUpViewModel.SignUpEvent.WeakPasswordException -> {
                        markErrorFields(listOf(SignUpViewModel.SignUpEvent.Fields.PASSWORD))
                        binding.textViewPasswordErrorMessage.apply {
                            text = event.reason
                            isVisible = true
                        }
                    }//WeakPasswordException

                    is SignUpViewModel.SignUpEvent.ExistingEmailException -> {
                        markErrorFields(listOf(SignUpViewModel.SignUpEvent.Fields.EMAIL))
                        binding.textViewPasswordErrorMessage.apply {
                            setText(R.string.existing_email)
                            isVisible = true
                        }
                    }//ExistingEmailException

                    is SignUpViewModel.SignUpEvent.InvalidEmailException -> {
                        markErrorFields(listOf(SignUpViewModel.SignUpEvent.Fields.EMAIL))
                        binding.textViewPasswordErrorMessage.apply {
                            setText(R.string.invalid_email)
                            isVisible = true
                        }
                    }//InvalidEmailException

                    is SignUpViewModel.SignUpEvent.NetworkException -> {
                        Snackbar.make(
                            view,
                            R.string.no_internet_connection,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Try again") {
                            viewModel.onButtonSignUpWithEmailClicked()
                        }
                            .show()
                    }//NetworkException

                    is SignUpViewModel.SignUpEvent.UnexpectedException -> {
                        binding.textViewPasswordErrorMessage.apply {
                            setText(R.string.unexpected_error)
                            isVisible = true
                        }
                    }//UnexpectedException

                    is SignUpViewModel.SignUpEvent.NavigateToHomeFragment -> {
                        val action = SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
                        navController.navigate(action)
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