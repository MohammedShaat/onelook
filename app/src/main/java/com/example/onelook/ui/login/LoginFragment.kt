package com.example.onelook.ui.login

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
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentLoginBinding
import com.example.onelook.util.PASSWORD_REST_EMAIL_REQ_KEY
import com.example.onelook.util.hideBottomNavigation
import com.example.onelook.util.hideSplashScreen
import com.example.onelook.util.isInternetAvailable
import com.example.onelook.util.onCollect
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
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var navController: NavController

    @Inject
    lateinit var signInClient: SignInClient

    @Inject
    @Named("login")
    lateinit var signInRequest: BeginSignInRequest
    private val googleSignInLauncher = setupGoogleSignInLauncher()

    private val callbackManager = CallbackManager.Factory.create()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        binding = FragmentLoginBinding.bind(view)
        navController = findNavController()

        // Listeners
        binding.apply {
            sendInputsDataWhenChanged()

            buttonLogin.setOnClickListener {
                viewModel.onButtonLoginWithEmailClicked()
            }

            imageButtonPasswordVisibility.setOnClickListener {
                viewModel.onPasswordVisibilityClicked()
            }

            textViewForgotYourPassword.setOnClickListener {
                viewModel.onForgetPasswordClicked()
            }

            textViewSignUp.setOnClickListener {
                viewModel.onSinUpClick()
            }

            imageButtonGoogle.setOnClickListener {
                viewModel.onButtonLoginWithGoogleClicked()
            }

            imageButtonFacebook.setOnClickListener {
                viewModel.onButtonLoginWithFacebookClicked()
            }
        }//Listeners

        // Observers
        viewModel.apply {
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
                    buttonLogin.isEnabled = !isLoading
                    imageButtonGoogle.isEnabled = !isLoading
                    imageButtonGoogle.isEnabled = !isLoading
                }
            }

            onCollect(singUpEvent) { event ->
                hideErrors()
                when (event) {
                    is LoginViewModel.LoginEvent.ShowEmptyFieldsMessage -> {
                        markErrorFields(event.fields)
                        binding.textViewErrorMessage.apply {
                            setText(R.string.fill_required_fields)
                            isVisible = true
                        }
                    }//ShowEmptyFieldsMessage

                    is LoginViewModel.LoginEvent.NavigateToSignUpFragment -> {
                        navController.popBackStack()
                        navController.navigate(R.id.signUpFragment)
                    }//NavigateToSignUpFragment

                    is LoginViewModel.LoginEvent.NavigateToPasswordReminder -> {
                        val action =
                            LoginFragmentDirections.actionLoginFragmentToPasswordReminder1Fragment()
                        navController.navigate(action)
                    }//NavigateToPasswordReminder

                    is LoginViewModel.LoginEvent.NavigateToHomeFragment -> {
                        val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                        navController.navigate(action)
                    }//NavigateToHomeFragment

                    is LoginViewModel.LoginEvent.ErrorOccurred -> {
                        Snackbar.make(
                            requireView(),
                            event.message ?: getString(R.string.unexpected_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }//ErrorOccurred

                    is LoginViewModel.LoginEvent.ShowSigningWithEmailFailedMessage -> {
                        markErrorFields(event.fields)
                        val textViewErrorMessage = binding.textViewErrorMessage
                        textViewErrorMessage.isVisible = true
                        when (event.exception) {
                            LoginViewModel.SigningWithEmailExceptions.NO_EXIST_USER -> {
                                textViewErrorMessage.setText(R.string.not_exist_email)
                            }
                            LoginViewModel.SigningWithEmailExceptions.WRONG_PASSWORD -> {
                                textViewErrorMessage.setText(R.string.wrong_password)
                            }
                            LoginViewModel.SigningWithEmailExceptions.TOO_MANY_REQUESTS -> {
                                textViewErrorMessage.setText(R.string.too_many_requests)
                            }
                            LoginViewModel.SigningWithEmailExceptions.NETWORK_ISSUE -> {
                                textViewErrorMessage.setText(R.string.no_connection)
                            }
                            LoginViewModel.SigningWithEmailExceptions.OTHER_EXCEPTIONS -> {
                                textViewErrorMessage.text =
                                    event.message ?: getString(R.string.unexpected_error_2)
                            }
                        }
                    }//ShowSigningWithEmailFailedMessage

                    is LoginViewModel.LoginEvent.ShowSigningWithProviderFailedMessage -> {
                        val msg = when (event.exception) {
                            LoginViewModel.SigningWithProviderExceptions.NETWORK_ISSUE ->
                                getString(R.string.no_connection)

                            LoginViewModel.SigningWithProviderExceptions.OTHER_EXCEPTIONS ->
                                event.message ?: getString(R.string.unexpected_error)
                        }
                        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
                    }//ShowSigningWithProviderFailedMessage

                    is LoginViewModel.LoginEvent.ShowUserNotFoundMessage -> {
                        Snackbar.make(
                            view,
                            R.string.account_not_registered_yet,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }//ShowUserNotFoundException

                    is LoginViewModel.LoginEvent.LoginWithGoogle -> {
                        loginWithGoogle()
                    }//LoginWithGoogle

                    is LoginViewModel.LoginEvent.LoginWithFacebook -> {
                        loginWithFacebook()
                    }//LoginWithFacebook
                }
            }
        }//Observers

        setFragmentResultListener(PASSWORD_REST_EMAIL_REQ_KEY) { _, bundle ->
            val receivedEmail = bundle.getString("email") ?: return@setFragmentResultListener
            Snackbar.make(
                view,
                getString(R.string.sent_password_reset_email, receivedEmail),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }//onViewCreated

    override fun onResume() {
        super.onResume()
        hideSplashScreen()
    }

    private fun sendInputsDataWhenChanged() {
        binding.apply {
            textInputEmail.addTextChangedListener { viewModel.email.value = it.toString() }
            textInputPassword.addTextChangedListener { viewModel.password.value = it.toString() }
        }
    }

    // Marks fields' labels that have error
    private fun markErrorFields(fields: List<LoginViewModel.Fields>) {
        fields.forEach { field ->
            when (field) {
                LoginViewModel.Fields.EMAIL -> binding.textViewEmail.setTextColor(
                    resources.getColor(R.color.alert)
                )
                LoginViewModel.Fields.PASSWORD -> binding.textViewPassword.setTextColor(
                    resources.getColor(R.color.alert)
                )
            }
        }
    }

    // Resets labels and hide error message
    private fun hideErrors() {
        binding.apply {
            textViewEmail.setTextColor(resources.getColor(R.color.dark_grey))
            textViewPassword.setTextColor(resources.getColor(R.color.dark_grey))
            textViewErrorMessage.visibility = View.INVISIBLE
        }
    }

    private fun loginWithGoogle() {
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
            .addOnFailureListener { e ->
                viewModel.isLoading(false)
                Timber.e("Login with Google failed\n $e")
                if (!isInternetAvailable()) {
                    Timber.e("No internet connection")
                    viewModel.onErrorOccurred(getString(R.string.no_connection))
                } else {
                    viewModel.onErrorOccurred(getString(R.string.no_registered_accounts))
                }
            }
    }

    private fun setupGoogleSignInLauncher(): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    val token = credential.googleIdToken ?: {
                        Timber.e("No token found")
                        viewModel.onErrorOccurred()
                    }
                    viewModel.onGoogleTokenReceived(credential)
                } catch (e: ApiException) {
                    Timber.e("No  credential found\n $e")
                    viewModel.onErrorOccurred(e.localizedMessage)
                }
            }
        }
    }

    private fun loginWithFacebook() {
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
                    Timber.i("Signing with Facebook succeeded")
                    viewModel.onFacebookTokenReceived(result.accessToken)
                }

                override fun onCancel() {
                    Timber.e("Signing with Facebook canceled")
                    if (!isInternetAvailable()) {
                        Timber.e("No internet connection")
                        viewModel.onErrorOccurred(getString(R.string.no_connection))
                    }
                }

                override fun onError(error: FacebookException) {
                    Timber.e("Signing with Facebook failed\n $error")
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
