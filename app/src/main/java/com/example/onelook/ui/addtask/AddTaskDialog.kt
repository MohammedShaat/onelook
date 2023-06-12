package com.example.onelook.ui.addtask

import android.app.ActionBar.LayoutParams
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.DialogAddTaskBinding
import com.example.onelook.util.onCollect

class AddTaskDialog : DialogFragment(R.layout.dialog_add_task) {

    private val viewModel: AddTaskViewModel by viewModels()
    private lateinit var binding: DialogAddTaskBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets up the width and background of dialog window
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_dialog)
            setLayout(
                (resources.displayMetrics.widthPixels * .8).toInt(),
                LayoutParams.WRAP_CONTENT
            )
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
                        val action = AddTaskDialogDirections.actionGlobalAddActivityFragment()
                        findNavController().navigate(action)
                    }//NavigateToAddActivityDialog

                    is AddTaskViewModel.AddTaskEvent.NavigateToAddSupplementDialog -> {
                        val action = AddTaskDialogDirections.actionGlobalAddSupplementFragment()
                        findNavController().navigate(action)
                    }//NavigateToAddSupplementDialog
                }
            }
        }
    }
}