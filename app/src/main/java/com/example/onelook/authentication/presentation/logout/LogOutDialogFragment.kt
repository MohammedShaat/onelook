package com.example.onelook.authentication.presentation.logout

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.DialogConfirmationBinding
import com.example.onelook.common.util.mainActivity
import com.example.onelook.common.util.onCollect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException

@AndroidEntryPoint
class LogOutDialogFragment : DialogFragment(R.layout.dialog_confirmation) {

    private val viewModel: LogOutDialogViewModel by viewModels()
    private var _binding: DialogConfirmationBinding? = null
    val binding: DialogConfirmationBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets up dialog frame
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_dialog)
        }

        _binding = DialogConfirmationBinding.bind(view)

        // Sets up buttons, title, and message
        binding.apply {
            textViewTitle.setText(R.string.dialog_title_log_out)
            textViewMessage.text = getString(R.string.dialog_message_log_out)
            buttonNegative.setText(R.string.dialog_button_negative_cancel)
            buttonPositive.setText(R.string.dialog_button_positive_log_out)
        }


        // Listeners
        binding.apply {

            buttonNegative.setOnClickListener {
                viewModel.onButtonNegativeClicked()
            }

            buttonPositive.setOnClickListener {
                viewModel.onButtonPositiveClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(isLoading) { isLoading ->
                binding.apply {
                    buttonNegative.isEnabled = !isLoading
                    buttonPositive.isEnabled = !isLoading
                    isCancelable = !isLoading
                    progressBar.isVisible = isLoading
                }
            }

            onCollect(logOutEvent) { event ->
                when (event) {
                    LogOutDialogViewModel.LogOutEvent.DismissDialog -> {
                        dismiss()
                    }//DismissDialog

                    LogOutDialogViewModel.LogOutEvent.NavigateToLogInFragmentAfterLoggingOut -> {
                        val action = LogOutDialogFragmentDirections.actionGlobalLoginFragment()
                        mainActivity.popAllFragmentsFromBackStack()
                        findNavController().navigate(action)
                    }//NavigateToSignInFragmentAfterLoggingOut

                    LogOutDialogViewModel.LogOutEvent.CancelActivityCoroutines -> {
                        mainActivity.cancelCoroutines(CancellationException("Logging Out"))
                    }//CancelActivityCoroutines
                }
            }
        }//Observers

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}