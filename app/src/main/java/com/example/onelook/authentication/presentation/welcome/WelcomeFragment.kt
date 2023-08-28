package com.example.onelook.authentication.presentation.welcome

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = this

        binding.buttonLetsStart.setOnClickListener {
            val action = WelcomeFragmentDirections.actionWelcomeFragmentToViewPagerFragment()
            findNavController().navigate(action)
        }
    }
}