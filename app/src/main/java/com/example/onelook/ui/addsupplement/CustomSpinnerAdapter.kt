package com.example.onelook.ui.addsupplement

import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SpinnerAdapter
import com.example.onelook.databinding.ItemSpinnerDropDownBinding
import com.example.onelook.databinding.ItemSpinnerSelectedBinding

class CustomSpinnerAdapter(
    private val items: List<SpinnerItem>,
    private val onClickListener: (Int) -> Unit = { }
) : SpinnerAdapter {
    override fun registerDataSetObserver(observer: DataSetObserver?) {}

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): SpinnerItem {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        onClickListener(position)
        val binding = if (convertView != null)
            ItemSpinnerSelectedBinding.bind(convertView)
        else
            ItemSpinnerSelectedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        binding.apply {
            textViewName.text = getItem(position).text
        }
        return binding.root
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun isEmpty(): Boolean {
        return count == 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        onClickListener(position)
        val binding = if (convertView != null)
            ItemSpinnerDropDownBinding.bind(convertView)
        else
            ItemSpinnerDropDownBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        binding.apply {
            textViewName.text = getItem(position).text
        }
        return binding.root
    }
}