package com.example.onelook.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.onelook.ui.mainactivity.MainActivity
import com.example.onelook.R
import com.example.onelook.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        (requireActivity() as MainActivity).showBottomNavigation()

        val userName = auth.currentUser?.displayName
        binding.textViewGreeting.text = getString(R.string.home_text_view_greeting, userName)

        val toolbar = binding.toolbar
        toolbar.apply {
            inflateMenu(R.menu.menu_fragment_home)
        }

    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).hideSplashScreen()
    }
}