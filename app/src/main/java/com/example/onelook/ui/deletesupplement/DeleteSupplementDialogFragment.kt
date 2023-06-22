package com.example.onelook.ui.deletesupplement

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.onelook.R
import com.example.onelook.databinding.DialogConfirmationBinding
import com.example.onelook.util.DELETE_SUPPLEMENT_REQ_KEY
import com.example.onelook.util.SUPPLEMENT_NAME_KEY
import com.example.onelook.util.onCollect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteSupplementDialogFragment : DialogFragment(R.layout.dialog_confirmation) {

    private val viewModel: DeleteSupplementViewModel by viewModels()
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
            textViewTitle.setText(R.string.dialog_title_delete)
            textViewMessage.text = getString(R.string.dialog_message_delete, "supplement")
            buttonNegative.setText(R.string.dialog_button_negative_cancel)
            buttonPositive.setText(R.string.dialog_button_positive_delete)
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

            onCollect(isDeleting) { isDeleting ->
                binding.apply {
                    buttonNegative.isEnabled = !isDeleting
                    buttonPositive.isEnabled = !isDeleting
                    isCancelable = !isDeleting
                    progressBar.isVisible = isDeleting
                }
            }

            onCollect(deleteSupplementEvent) { event ->
                when (event) {
                    DeleteSupplementViewModel.DeleteSupplementEvent.DismissDialog -> {
                        dismiss()
                    }//DismissDialog

                    is DeleteSupplementViewModel.DeleteSupplementEvent.NavigateBackAfterSupplementDeleted -> {
                        setFragmentResult(DELETE_SUPPLEMENT_REQ_KEY, Bundle().apply {
                            putString(SUPPLEMENT_NAME_KEY, event.supplementName)
                        })
                        dismiss()
                    }//NavigateBackAfterSupplementDeleted
                }
            }
        }//Observers
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}