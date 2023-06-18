package com.example.onelook.ui.supplements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.onelook.data.domain.Supplement
import com.example.onelook.databinding.ItemSupplementActivityEditBinding
import com.example.onelook.ui.home.supplementIcon

class SupplementAdapter :
    ListAdapter<Supplement, SupplementAdapter.SupplementVH>(SupplementDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplementVH {
        val binding =
            ItemSupplementActivityEditBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return SupplementVH(binding)
    }

    override fun onBindViewHolder(holder: SupplementVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SupplementVH(private val binding: ItemSupplementActivityEditBinding) :
        ViewHolder(binding.root) {

        fun bind(supplement: Supplement) {
            binding.apply {
                imageViewIcon.supplementIcon(supplement.formattedForm)
                textViewName.text = supplement.form.replaceFirstChar { it.uppercase() }
            }
        }
    }

    class SupplementDiff : DiffUtil.ItemCallback<Supplement>() {
        override fun areItemsTheSame(oldItem: Supplement, newItem: Supplement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Supplement, newItem: Supplement): Boolean {
            return oldItem == newItem
        }
    }
}