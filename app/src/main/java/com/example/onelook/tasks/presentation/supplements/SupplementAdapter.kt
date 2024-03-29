package com.example.onelook.tasks.presentation.supplements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.databinding.ItemSupplementActivityEditBinding
import com.example.onelook.tasks.presentation.home.supplementIcon

class SupplementAdapter(
    private val onEditClickListener: (Supplement) -> Unit,
    private val onDeleteClickListener: (Supplement) -> Unit
) :
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

        init {
            binding.imageButtonEdit.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onEditClickListener(getItem(adapterPosition))
            }

            binding.imageButtonDelete.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onDeleteClickListener(getItem(adapterPosition))
            }
        }

        fun bind(supplement: Supplement) {
            binding.apply {
                imageViewIcon.supplementIcon(supplement.parsedForm)
                textViewName.text = supplement.name.replaceFirstChar { it.uppercase() }
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