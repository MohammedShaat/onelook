package com.example.onelook.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow

fun <T> Fragment.onCollect(flow: Flow<T>, block: (value: T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        flow.collect(block)
    }
}