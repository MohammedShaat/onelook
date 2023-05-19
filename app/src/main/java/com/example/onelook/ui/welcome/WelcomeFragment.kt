package com.example.onelook.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.onelook.R
import com.example.onelook.databinding.FragmentWelcomeBinding
import timber.log.Timber

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = this

        binding.buttonLetsStart.setOnClickListener {
            Timber.i("Let's start: clicked")
            val action = WelcomeFragmentDirections.actionWelcomeFragmentToViewPagerFragment()
            findNavController().navigate(action)
        }
    }
}