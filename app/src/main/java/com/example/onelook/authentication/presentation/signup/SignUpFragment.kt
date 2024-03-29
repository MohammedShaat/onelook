package com.example.onelook.authentication.presentation.signup

import android.app.Activity
import android.content.ActivityNotFoundException
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
import com.example.onelook.common.util.hideBottomNavigation
import com.example.onelook.common.util.isInternetAvailable
import com.example.onelook.common.util.onCollect
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
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

    private val googleSignInLauncher = setupGoogleSignInLauncher()

    private val callbackManager = CallbackManager.Factory.create()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        // Setup view binding and navController
        binding = FragmentSignUpBinding.bind(view)
        navController = findNavController()

        viewModel.onSignUpVisited()

        markErrorFields(viewModel.errorFields)


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
                viewModel.onButtonLoginClicked()
            }

            imageButtonGoogle.setOnClickListener {
                viewModel.onButtonSignUpWithGoogleClicked()
            }

            imageButtonFacebook.setOnClickListener {
                viewModel.onButtonSignUpWithFacebookClicked()
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
                        textInputPassword.setSelection(textInputPassword.text.length)
                        imageButtonPasswordVisibility.setImageResource(R.drawable.ic_password_visible)
                    }

                } else {
                    binding.apply {
                        textInputPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        textInputPassword.setSelection(textInputPassword.text.length)
                        imageButtonPasswordVisibility.setImageResource(R.drawable.ic_password_invisible)
                    }
                }
            }

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.apply {
                    progressBar.isVisible = isLoading
                    buttonSignUp.isEnabled = !isLoading && viewModel.buttonSignUpEnabled.value!!
                    imageButtonGoogle.isEnabled = !isLoading
                    imageButtonGoogle.isEnabled = !isLoading
                }
            }

            onCollect(singUpEvent) { event ->
                when (event) {

                    is SignUpViewModel.SignUpEvent.HideErrors -> {
                        hideErrors()
                    }//HideErrors

                    is SignUpViewModel.SignUpEvent.ShowEmptyFieldsMessage -> {
                        markErrorFields(viewModel.errorFields)
                        binding.textViewErrorMessage.apply {
                            setText(R.string.fill_required_fields)
                            isVisible = true
                        }
                        viewModel.onErrorMessageChanged(binding.textViewErrorMessage.text.toString())
                    }//EmptyFields

                    is SignUpViewModel.SignUpEvent.NavigateToLoginFragment -> {
                        navController.popBackStack()
                        navController.navigate(R.id.loginFragment)
                    }//NavigateToLoginFragment

                    is SignUpViewModel.SignUpEvent.ShowCreationWithEmailFailedMessage -> {
                        markErrorFields(viewModel.errorFields)
                        val textViewErrorMessage = binding.textViewErrorMessage
                        textViewErrorMessage.isVisible = true
                        when (event.exception) {
                            SignUpViewModel.CreationWithEmailExceptions.WEAK_PASSWORD -> {
                                textViewErrorMessage.text = event.message
                            }

                            SignUpViewModel.CreationWithEmailExceptions.INVALID_EMAIL -> {
                                textViewErrorMessage.setText(R.string.invalid_email)
                            }

                            SignUpViewModel.CreationWithEmailExceptions.EXISTING_EMAIL -> {
                                textViewErrorMessage.setText(R.string.existing_email)
                            }

                            SignUpViewModel.CreationWithEmailExceptions.NETWORK_ISSUE -> {
                                textViewErrorMessage.setText(R.string.no_connection)
                            }

                            SignUpViewModel.CreationWithEmailExceptions.OTHER_EXCEPTIONS -> {
                                textViewErrorMessage.text =
                                    event.message ?: getString(R.string.unexpected_error_2)
                            }
                        }
                        viewModel.onErrorMessageChanged(binding.textViewErrorMessage.text.toString())
                    }//ShowCreationWithEmailFailed

                    is SignUpViewModel.SignUpEvent.NavigateToHomeFragment -> {
                        val action = SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
                        navController.navigate(action)
                    }//NavigateToHomeFragment

                    is SignUpViewModel.SignUpEvent.ErrorOccurred -> {
                        Snackbar.make(
                            requireView(),
                            event.message ?: getString(R.string.unexpected_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }//ErrorOccurred

                    is SignUpViewModel.SignUpEvent.ShowCreationWithProviderFailedMessage -> {
                        val msg = when (event.exception) {
                            SignUpViewModel.CreationWithProviderExceptions.NETWORK_ISSUE ->
                                getString(R.string.no_connection)

                            SignUpViewModel.CreationWithProviderExceptions.OTHER_EXCEPTIONS ->
                                event.message ?: getString(R.string.unexpected_error)

                            SignUpViewModel.CreationWithProviderExceptions.EXISTING_EMAIL ->
                                event.message ?: getString(R.string.account_already_registered)
                        }
                        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
                    }//ShowCreationWithProviderFailedMessage

                    is SignUpViewModel.SignUpEvent.SignUpWithGoogle -> {
                        signUpWithGoogle()
                    }//SignUpWithGoogle

                    is SignUpViewModel.SignUpEvent.SignUpWithFacebook -> {
                        signUpWithFacebook()
                    }//SignUpWithFacebook
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
            if (fields.isNotEmpty())
                binding.textViewErrorMessage.apply {
                    text = viewModel.errorMessage
                    isVisible = true
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
            textViewErrorMessage.isVisible = false
        }
    }

    private fun signUpWithGoogle() {
        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    googleSignInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    )
                } catch (e: ActivityNotFoundException) {
                    Timber.e("Couldn't start One Tap UI\n $e")
                    viewModel.onErrorOccurred()
                }
                viewModel.isLoading(false)
            }
            .addOnFailureListener() { e ->
                viewModel.isLoading(false)
                Timber.e("Sign in request failed\n $e")
                if (!isInternetAvailable()) {
                    Timber.e("No internet connection")
                    viewModel.onErrorOccurred(getString(R.string.no_connection))
                } else {
                    viewModel.onErrorOccurred(e.localizedMessage)
                }
            }
    }

    private fun setupGoogleSignInLauncher(): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    credential.googleIdToken ?: {
                        Timber.e("No token found\n")
                        viewModel.onErrorOccurred()
                    }//token == null
                    viewModel.onGoogleTokenReceived(credential)
                } catch (e: ApiException) {
                    Timber.e("No credential found\n $e")
                    viewModel.onErrorOccurred(e.localizedMessage)
                }//no credential
            }
        }
    }

    private fun signUpWithFacebook() {
        val loginManager = LoginManager.getInstance()
        loginManager.logInWithReadPermissions(
            this,
            callbackManager,
            listOf("email", "public_profile")
        )
        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Timber.i("Facebook login succeeded")
                    viewModel.onFacebookTokenReceived(result.accessToken)
                }

                override fun onCancel() {
                    Timber.i("Facebook login canceled")
                    if (!isInternetAvailable()) {
                        Timber.e("No internet connection")
                        viewModel.onErrorOccurred(getString(R.string.no_connection))
                    }
                }

                override fun onError(error: FacebookException) {
                    Timber.e("Facebook login failed\n $error")
                    if (!isInternetAvailable()) {
                        Timber.e("No internet connection")
                        viewModel.onErrorOccurred(getString(R.string.no_connection))
                    } else {
                        viewModel.onErrorOccurred(error.localizedMessage)
                    }
                }
            }
        )
        viewModel.isLoading(false)
    }
}