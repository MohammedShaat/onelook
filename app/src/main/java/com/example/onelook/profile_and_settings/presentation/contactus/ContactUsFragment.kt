package com.example.onelook.profile_and_settings.presentation.contactus

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentContactUsBinding
import com.example.onelook.common.util.hideBottomNavigation
import com.example.onelook.common.util.onCollect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactUsFragment : Fragment(R.layout.fragment_contact_us) {

    private val viewModel: ContactUsViewModel by viewModels()
    private var _binding: FragmentContactUsBinding? = null
    private val binding: FragmentContactUsBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        _binding = FragmentContactUsBinding.bind(view)


        // Listeners
        binding.apply {

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            textViewPhoneNumber.setOnClickListener {
                viewModel.onPhoneNumberClicked()
            }

            textViewPhoneNumber.setOnLongClickListener {
                viewModel.onPhoneNumberClickedLong()
                true
            }

            textViewEmail.setOnClickListener {
                viewModel.onEmailClicked()
            }

            textViewEmail.setOnLongClickListener {
                viewModel.onEmailClickedLong()
                true
            }

            linearLayoutLocation.setOnClickListener {
                viewModel.onLocationClicked()
            }

            linearLayoutLocation.setOnLongClickListener {
                viewModel.onLocationClickedLong()
                true
            }
        }//Listeners


        // Observers
        viewModel.apply {

            onCollect(contactUsEvent) { event ->
                when (event) {
                    ContactUsViewModel.ContactUsEvent.DialPhoneNumber -> {
                        openDialerApp()
                    }//DialPhoneNumber

                    ContactUsViewModel.ContactUsEvent.CopyPhoneNumber -> {
                        copyToClipboard("Phone number", getString(R.string.onelook_phone_number))
                    }//CopyPhoneNumber

                    ContactUsViewModel.ContactUsEvent.SendEmail -> {
                        sendEmailToOneLook()
                    }//SendEmail

                    ContactUsViewModel.ContactUsEvent.CopyEmail -> {
                        copyToClipboard("Email", getString(R.string.onelook_email))
                    }//CopyEmail

                    ContactUsViewModel.ContactUsEvent.DisplayLocation -> {
                        displayLocation()
                    }//DisplayLocation

                    ContactUsViewModel.ContactUsEvent.CopyLocation -> {
                        copyToClipboard("Location", getString(R.string.onelook_location))
                    }//CopyLocation
                }
            }
        }//Observers
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboardManager =
            ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                    as ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(context, getString(R.string.copied_to_clipbaord, text), Toast.LENGTH_SHORT)
            .show()
    }

    private fun openDialerApp() {
        val phoneNumber = getString(R.string.onelook_phone_number)
        val phoneNumberIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(phoneNumberIntent)
    }

    private fun sendEmailToOneLook() {
        val email = getString(R.string.onelook_email)
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        startActivity(emailIntent)
    }

    private fun displayLocation() {
        val latitude = 35.4797
        val longitude = -97.5381
        val mapUri = Uri.parse("geo:$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        }
    }
}