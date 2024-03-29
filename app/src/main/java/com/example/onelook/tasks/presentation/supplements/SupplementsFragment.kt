package com.example.onelook.tasks.presentation.supplements

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.timer.data.local.Timer
import com.example.onelook.databinding.FragmentSupplementsBinding
import com.example.onelook.common.util.ADD_SUPPLEMENT_REQ_KEY
import com.example.onelook.common.util.Resource
import com.example.onelook.common.util.DELETE_SUPPLEMENT_REQ_KEY
import com.example.onelook.common.util.SUPPLEMENT_NAME_KEY
import com.example.onelook.common.util.UPDATE_SUPPLEMENT_REQ_KEY
import com.example.onelook.common.util.capital
import com.example.onelook.common.util.onCollect
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.net.UnknownHostException

@AndroidEntryPoint
class SupplementsFragment : Fragment(R.layout.fragment_supplements) {
    private val viewModel: SupplementsViewModel by viewModels()
    private var _binding: FragmentSupplementsBinding? = null
    private val binding: FragmentSupplementsBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSupplementsBinding.bind(view)

        // Populates supplements list recyclerView
        val supplementsAdapter = SupplementAdapter(
            viewModel::onEditSupplementClicked,
            viewModel::onDeleteSupplementClicked
        )
        binding.recyclerViewSupplementsList.apply {
            setHasFixedSize(true)
            adapter = supplementsAdapter
        }

        // Sets up swipe refresh layout and set listener
        setupAndHandleSwipeRefreshLayout()

        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onSwipeRefreshSwiped()
            }

            buttonAddSupplement.setOnClickListener {
                viewModel.onButtonAddSupplementClicked()
            }
        }//Listeners


        // Observers
        viewModel.apply {

            // Supplements list
            onCollect(supplements) { result ->
                supplementsAdapter.submitList(result.data)
                binding.textViewNoData.isVisible =
                    result.data.isNullOrEmpty() && (result !is Resource.Loading && !Timer.isSyncing.value)
            }

            // Refreshing indicator
            onCollect(isRefreshing) { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }

            // Events
            onCollect(supplementsEvent) { event ->
                when (event) {
                    SupplementsViewModel.SupplementsEvent.NavigateToAddEditSupplementFragment -> {
                        val action =
                            SupplementsFragmentDirections.actionGlobalAddEditSupplementFragment(null)
                        findNavController().navigate(action)
                    }//NavigateToAddSupplementFragment

                    is SupplementsViewModel.SupplementsEvent.ShowRefreshFailedMessage -> {
                        val msg = when (event.exception) {
                            is UnknownHostException -> R.string.no_connection
                            else -> R.string.unexpected_error
                        }
                        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.buttonAddSupplement)
                            .show()
                    }//ShowRefreshFailedMessage

                    is SupplementsViewModel.SupplementsEvent.NavigateToAddEditSupplementFragmentForEditing -> {
                        val action =
                            SupplementsFragmentDirections.actionGlobalAddEditSupplementFragment(
                                event.supplement
                            )
                        findNavController().navigate(action)
                    }//NavigateToAddEditSupplementFragmentForEditing

                    is SupplementsViewModel.SupplementsEvent.NavigateToDeleteSupplementDialogFragment -> {
                        val action =
                            SupplementsFragmentDirections.actionSupplementsFragmentToDeleteSupplementDialogFragment(
                                event.supplement
                            )
                        findNavController().navigate(action)
                    }//NavigateToDeleteSupplementFragmentDialog
                }
            }
        }//Observers

        // Shows supplement created successfully snackBar
        setFragmentResultListener(ADD_SUPPLEMENT_REQ_KEY) { _, bundle ->
            val supplementName = bundle.getString(SUPPLEMENT_NAME_KEY)?.capital
            Snackbar.make(
                view,
                getString(R.string.supplement_added, supplementName),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(binding.buttonAddSupplement)
                .show()
        }

        // Shows supplement updated successfully snackBar
        setFragmentResultListener(UPDATE_SUPPLEMENT_REQ_KEY) { _, bundle ->
            val supplementName = bundle.getString(SUPPLEMENT_NAME_KEY)?.capital
            Snackbar.make(
                view,
                getString(R.string.supplement_updated, supplementName),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(binding.buttonAddSupplement)
                .show()
        }

        // Shows supplement deleted successfully snackBar
        setFragmentResultListener(DELETE_SUPPLEMENT_REQ_KEY) { _, bundle ->
            val supplementName = bundle.getString(SUPPLEMENT_NAME_KEY)?.capital
            Snackbar.make(
                view,
                getString(R.string.supplement_deleted, supplementName),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(binding.buttonAddSupplement)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupAndHandleSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(
                ResourcesCompat.getColor(resources, R.color.turquoise, null),
                ResourcesCompat.getColor(resources, R.color.purple_plum, null),
            )
        }
    }
}