package com.example.onelook.ui.onboarding.screens

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.onelook.R
import com.example.onelook.databinding.FragmentOnboardingBinding
import com.example.onelook.ui.onboarding.ViewPagerFragment
import com.example.onelook.ui.onboarding.ViewPagerFragmentDirections

class Onboarding3Fragment : Fragment(R.layout.fragment_onboarding) {

    private lateinit var binding: FragmentOnboardingBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentOnboardingBinding.bind(view)

        setupContent(
            R.drawable.yoga,
            R.string.onboarding_3_text_view_title,
            R.string.onboarding_3_text_view_subtitle
        )

        val viewPagerFragment = requireParentFragment() as ViewPagerFragment
        viewPager = viewPagerFragment.getViewPager()

        binding.buttonNext.setOnClickListener {
            val action = ViewPagerFragmentDirections.actionViewPagerFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        binding.buttonSkipIntro.setOnClickListener {
            val action = ViewPagerFragmentDirections.actionViewPagerFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    }

    private fun setupContent(pic: Int, title: Int, subtitle: Int) {
        binding.apply {
            imageViewPic.setImageResource(pic)
            textViewTitle.setText(title)
            textViewSubtitle.setText(subtitle)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        onBackPressedCallback.remove()
    }
}