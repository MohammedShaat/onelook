package com.example.onelook.ui.signup

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentSignUpBinding
import com.example.onelook.util.onCollect
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private lateinit var binding: FragmentSignUpBinding
    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var navController: NavController

    @Inject
    lateinit var signInClient: SignInClient

    @Inject
    @Named("sign_up")
    lateinit var signInRequest: BeginSignInRequest

    private val signInActivityResult = setupSignInActivityResult()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup view binding and navController
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
                viewModel.onButtonSignUpWithGoogleClicked()
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
                    buttonSignUp.isEnabled = !isLoading && viewModel.buttonSignUpEnabled.value!!
                    progressBar.isVisible = isLoading
                }
            }

            onCollect(singUpEvent) { event ->
                when (event) {
                    is SignUpViewModel.SignUpEvent.ShowEmptyFieldsMessage -> {
                        markErrorFields(event.fields)
                        binding.textViewPasswordErrorMessage.apply {
                            setText(R.string.empty_fields)
                            isVisible = true
                        }
                    }//EmptyFields

                    is SignUpViewModel.SignUpEvent.HideErrors -> {
                        hideErrors()
                    }//HideErrors

                    is SignUpViewModel.SignUpEvent.NavigateToLoginFragment -> {
                        navController.popBackStack()
                        navController.navigate(R.id.loginFragment)
                    }//NavigateToLoginFragment

                    is SignUpViewModel.SignUpEvent.ShowCreationWithEmailFailedMessage -> {
                        markErrorFields(event.fields)
                        val textViewPasswordErrorMessage = binding.textViewPasswordErrorMessage
                        textViewPasswordErrorMessage.isVisible = true
                        when (event.exception) {
                            SignUpViewModel.CreationWithEmailExceptions.WEAK_PASSWORD -> {
                                textViewPasswordErrorMessage.text = event.message
                            }
                            SignUpViewModel.CreationWithEmailExceptions.INVALID_EMAIL -> {
                                textViewPasswordErrorMessage.setText(R.string.invalid_email)
                            }
                            SignUpViewModel.CreationWithEmailExceptions.EXISTING_EMAIL -> {
                                textViewPasswordErrorMessage.setText(R.string.existing_email)
                            }
                            SignUpViewModel.CreationWithEmailExceptions.NETWORK_ISSUE -> {
                                textViewPasswordErrorMessage.setText(R.string.network_issue)
                            }
                            SignUpViewModel.CreationWithEmailExceptions.OTHER_EXCEPTIONS -> {
                                textViewPasswordErrorMessage.setText(R.string.unexpected_error_2)
                            }
                        }
                    }//ShowCreationWithEmailFailed

                    is SignUpViewModel.SignUpEvent.NavigateToHomeFragment -> {
                        val action = SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
                        navController.navigate(action)
                    }//NavigateToHomeFragment

                    is SignUpViewModel.SignUpEvent.SignUpWithGoogle -> {
                        signUpWithGoogle()
                    }//SignUpWithGoogle

                    is SignUpViewModel.SignUpEvent.ErrorOccurred -> {
                        Snackbar.make(
                            requireView(),
                            getString(event.id ?: R.string.unexpected_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }//ErrorOccurred

                    is SignUpViewModel.SignUpEvent.ShowCreationWithProviderFailedMessage -> {
                        Snackbar.make(
                            requireView(),
                            when (event.exception) {
                                SignUpViewModel.CreationWithProviderExceptions.NETWORK_ISSUE -> R.string.network_issue
                                SignUpViewModel.CreationWithProviderExceptions.OTHER_EXCEPTIONS -> R.string.unexpected_error
                            },
                            Snackbar.LENGTH_LONG
                        ).show()
                    }//CreationWithProviderExceptions
                }//when
            }
        }//Observers
    }//onViewCreated

    // Marks fields' labels that have error
    private fun markErrorFields(fields: List<SignUpViewModel.Fields>) {
        fields.forEach { field ->
            when (field) {
                SignUpViewModel.Fields.NAME -> binding.textViewName.setTextColor(
                    resources.getColor(R.color.alert)
                )
                SignUpViewModel.Fields.EMAIL -> binding.textViewEmail.setTextColor(
                    resources.getColor(R.color.alert)
                )
                SignUpViewModel.Fields.PASSWORD -> binding.textViewPassword.setTextColor(
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

    private fun signUpWithGoogle() {
        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    signInActivityResult.launch(
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    )
                } catch (e: SendIntentException) {
                    Timber.e("Couldn't start One Tap UI\n $e")
                }
            }
            .addOnFailureListener { e ->
                if (e is ApiException && e.statusCode == 16) {
                    Timber.e("No matching credentials found")
                    viewModel.onErrorOccurred(R.string.no_google_accounts)
                } else {
                    Timber.e("Unexpected exception happened\n $e")
                    viewModel.onErrorOccurred()
                }
            }
    }

    private fun setupSignInActivityResult(): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    credential.googleIdToken ?: {
                        Timber.e("No token found\n")
                        viewModel.onErrorOccurred()
                    }//token == null
                    viewModel.onSignInCredentialReceived(credential)
                } catch (e: ApiException) {
                    Timber.e("No credential found\n $e")
                    viewModel.onErrorOccurred()
                }//no credential
            }
        }
    }
}