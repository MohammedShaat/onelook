package com.example.onelook.ui.changename

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentChangeNameBinding
import com.example.onelook.util.CHANGE_NAME_REQ_KEY
import com.example.onelook.util.PASSWORD_REST_EMAIL_REQ_KEY
import com.example.onelook.util.onCollect
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeNameFragment : Fragment(R.layout.fragment_change_name) {

    private val viewModel: ChangeNameViewModel by viewModels()
    private var _binding: FragmentChangeNameBinding? = null
    private val binding: FragmentChangeNameBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentChangeNameBinding.bind(view)


        // Listeners
        binding.apply {

            textInputName.addTextChangedListener { newEditable ->
                viewModel.onNameChanged(newEditable.toString())
            }

            buttonCancel.setOnClickListener {
                viewModel.onCancelClicked()
            }

            buttonConfirmChanges.setOnClickListener {
                viewModel.onConfirmChangesClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            name.observe(viewLifecycleOwner) { newName ->
                binding.textInputName.apply {
                    if (newName == text.toString()) return@apply
                    setText(newName)
                }
            }

            isErrorVisible.observe(viewLifecycleOwner) { isErrorVisible ->
                if (isErrorVisible)
                    markErrorEmailField()
                else
                    hideError()
            }

            onCollect(isLoading) { isLoading ->
                binding.apply {
                    progressBar.isVisible = isLoading
                    buttonConfirmChanges.isEnabled = !isLoading
                    buttonCancel.isEnabled = !isLoading
                }
            }

            onCollect(changeNameEvent) { event ->
                hideError()
                when (event) {
                    ChangeNameViewModel.ChangeNameEvent.NavigateBack -> {
                        findNavController().popBackStack()
                    }//NavigateBack

                    ChangeNameViewModel.ChangeNameEvent.ShowEmptyNameFieldMessage -> {
                        binding.textViewMessage.setText(R.string.empty_email_field)
                        markErrorEmailField()
                    }//ShowEmptyNameFieldMessage

                    ChangeNameViewModel.ChangeNameEvent.NavigateBackAfterNameUpdated -> {
                        setFragmentResult(CHANGE_NAME_REQ_KEY, Bundle())
                        findNavController().popBackStack()
                    }//NavigateBackAfterNameUpdated

                    is ChangeNameViewModel.ChangeNameEvent.ShowNameChangingFailedMessage -> {
                        val message = when (event.exception) {
                            is FirebaseNetworkException -> R.string.no_connection
                            else -> R.string.unexpected_error
                        }
                        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
                    }//ShowNameChangingFailedMessage
                }
            }
        }//Observers
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideError() {
        viewModel.onErrorVisibilityChanged(false)
        binding.apply {
            textViewName.setTextColor(resources.getColor(R.color.dark_grey))
            textViewMessage.isVisible = false
        }
    }

    private fun markErrorEmailField() {
        viewModel.onErrorVisibilityChanged(true)
        binding.textViewName.setTextColor(resources.getColor(R.color.alert))
        binding.textViewMessage.isVisible = true
    }
}