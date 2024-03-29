package com.example.onelook.tasks.presentation.addtask

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.DialogAddTaskBinding
import com.example.onelook.common.util.onCollect

class AddTaskDialog : DialogFragment(R.layout.dialog_add_task) {

    private val viewModel: AddTaskViewModel by viewModels()
    private lateinit var binding: DialogAddTaskBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets up the width and background of dialog window
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_dialog)
//            setLayout(
//                (resources.displayMetrics.widthPixels * .8).toInt(),
//                LayoutParams.WRAP_CONTENT
//            )
        }

        binding = DialogAddTaskBinding.bind(view)

        // Listeners
        binding.apply {
            buttonActivity.setOnClickListener {
                viewModel.onButtonActivityClicked()
            }

            buttonSupplement.setOnClickListener {
                viewModel.onButtonSupplementClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {
            onCollect(addTaskEvent) { event ->
                when (event) {
                    is AddTaskViewModel.AddTaskEvent.NavigateToAddActivityDialog -> {
                        val action =
                            AddTaskDialogDirections.actionGlobalAddEditActivityFragment(null)
                        findNavController().navigate(action)
                    }//NavigateToAddActivityDialog

                    is AddTaskViewModel.AddTaskEvent.NavigateToAddSupplementDialog -> {
                        val action =
                            AddTaskDialogDirections.actionGlobalAddEditSupplementFragment(null)
                        findNavController().navigate(action)
                    }//NavigateToAddSupplementDialog
                }
            }
        }
    }
}