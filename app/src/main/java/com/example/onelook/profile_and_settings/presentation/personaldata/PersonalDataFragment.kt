package com.example.onelook.profile_and_settings.presentation.personaldata

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentPersonalDataBinding
import com.example.onelook.authentication.presentation.onboarding.ViewPagerFragmentDirections
import com.example.onelook.common.util.CHANGE_NAME_REQ_KEY
import com.example.onelook.common.util.hideBottomNavigation
import com.example.onelook.common.util.onCollect
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonalDataFragment : Fragment(R.layout.fragment_personal_data) {

    private val viewModel: PersonalDataViewModel by viewModels()
    private var _binding: FragmentPersonalDataBinding? = null
    private val binding: FragmentPersonalDataBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        _binding = FragmentPersonalDataBinding.bind(view)

        // Sets up data
        binding.apply {
            // Name
            textViewName.text = viewModel.name
            // Email
            emailContainer.isVisible = !viewModel.email.isNullOrEmpty()
            textViewEmail.text = viewModel.email
            buttonChangePassword.isVisible = !viewModel.email.isNullOrEmpty()
        }


        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            imageButtonEditName.setOnClickListener {
                viewModel.onEditNameClicked()
            }

            buttonChangePassword.setOnClickListener {
                viewModel.onChangePasswordClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(isLoading) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }

            onCollect(personalDataEvent) { event ->
                when (event) {
                    PersonalDataViewModel.PersonalDataEvent.NavigateToChangeNameFragment -> {
                        val action =
                            PersonalDataFragmentDirections.actionPersonalDataFragmentToChangeNameFragment(
                                viewModel.name
                            )
                        findNavController().navigate(action)
                    }//NavigateToChangeNameFragment

                    is PersonalDataViewModel.PersonalDataEvent.ShowNameChangingFailedMessage -> {
                        val message = when (event.exception) {
                            is FirebaseNetworkException -> R.string.no_connection
                            else -> R.string.unexpected_error
                        }
                        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
                    }//ShowNameChangingFailedMessage

                    PersonalDataViewModel.PersonalDataEvent.ShowPasswordRestEmailSentMessage -> {
                        val message = getString(R.string.sent_password_reset_email, viewModel.email)
                        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
                    }//ShowPasswordRestEmailSentMessage
                }
            }
        }//Observers

        setFragmentResultListener(CHANGE_NAME_REQ_KEY) { _, _ ->
            Snackbar.make(view, R.string.your_name_changed, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}