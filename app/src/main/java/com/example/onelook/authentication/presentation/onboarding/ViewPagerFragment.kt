package com.example.onelook.authentication.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.onelook.R
import com.example.onelook.databinding.FragmentViewPagerBinding
import com.example.onelook.authentication.presentation.onboarding.screens.Onboarding1Fragment
import com.example.onelook.authentication.presentation.onboarding.screens.Onboarding2Fragment
import com.example.onelook.authentication.presentation.onboarding.screens.Onboarding3Fragment

class ViewPagerFragment : Fragment(R.layout.fragment_view_pager) {

    private lateinit var binding: FragmentViewPagerBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = viewLifecycleOwner

        val fragments = listOf(
            Onboarding1Fragment(),
            Onboarding2Fragment(),
            Onboarding3Fragment(),
        )

        val viewPager = binding.viewPager
        viewPager.apply {
            adapter = ViewPagerAdapter(childFragmentManager, lifecycle, fragments)
        }

        binding.indicatorView.apply {
            setupWithViewPager(viewPager)
            setSliderWidth(resources.getDimension(R.dimen.slider_width))
            setSliderHeight(resources.getDimension(R.dimen.slider_height))

        }
    }

    fun getViewPager() = binding.viewPager
}