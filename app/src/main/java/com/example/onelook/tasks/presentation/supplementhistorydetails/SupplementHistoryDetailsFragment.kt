package com.example.onelook.tasks.presentation.supplementhistorydetails

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentSupplementHistoryDetailsBinding
import com.example.onelook.tasks.presentation.home.supplementIcon
import com.example.onelook.common.util.hideBottomNavigation
import com.example.onelook.common.util.hideSplashScreen
import com.example.onelook.common.util.onCollect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplementHistoryDetailsFragment : Fragment(R.layout.fragment_supplement_history_details) {

    private val viewModel: SupplementHistoryDetailsViewModel by viewModels()
    private var _binding: FragmentSupplementHistoryDetailsBinding? = null
    val binding: FragmentSupplementHistoryDetailsBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        _binding = FragmentSupplementHistoryDetailsBinding.bind(view)

        // Sets up icon and toolbar's title
        binding.apply {
            imageViewIcon.supplementIcon(viewModel.supplementHistory.value!!.parsedForm)
            toolBar.title = viewModel.supplementHistory.value!!.name
        }

        // Sets up dosages list recyclerView
        val dosageAdapter = DosageAdapter(viewModel::onCheckboxDosageChanged)
        binding.recyclerViewDosagesList.apply {
            setHasFixedSize(true)
            adapter = dosageAdapter
        }

        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            buttonEditSupplementHistory.setOnClickListener {
                viewModel.onButtonEditSupplementHistoryClicked()
            }
        }//Listeners

        // Observers
        viewModel.apply {

            onCollect(dosagesList) { dosagesList ->
                dosageAdapter.submitList(dosagesList)
            }

            onCollect(isLoading) { isLoading ->
                binding.apply {
                    buttonEditSupplementHistory.isEnabled = !isLoading
                    progressBar.isVisible = isLoading
                }
            }

            onCollect(supplementHistoryDetailsEvent) { event ->
                when (event) {
                    SupplementHistoryDetailsViewModel.SupplementHistoryDetailsEvent.NavigateBackAfterSupplementHistoryUpdated -> {
                        findNavController().popBackStack()
                    }//NavigateBackAfterSupplementHistoryUpdated
                }
            }
        }//Observers
    }

    override fun onResume() {
        super.onResume()
        hideSplashScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}