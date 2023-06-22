package com.example.onelook.ui.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.onelook.data.domain.DomainActivity
import com.example.onelook.databinding.ItemSupplementActivityEditBinding
import com.example.onelook.ui.home.activityIcon

class ActivityAdapter(
    private val onEditClickListener: (DomainActivity) -> Unit,
    private val onDeleteClickListener: (DomainActivity) -> Unit
) :
    ListAdapter<DomainActivity, ActivityAdapter.DomainActivityVH>(DomainActivityDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DomainActivityVH {
        val binding =
            ItemSupplementActivityEditBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DomainActivityVH(binding)
    }

    override fun onBindViewHolder(holder: DomainActivityVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DomainActivityVH(private val binding: ItemSupplementActivityEditBinding) :
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

        fun bind(activity: DomainActivity) {
            binding.apply {
                imageViewIcon.activityIcon(activity.parsedType)
                textViewName.text = activity.type.replaceFirstChar { it.uppercase() }
            }
        }
    }

    class DomainActivityDiff : DiffUtil.ItemCallback<DomainActivity>() {
        override fun areItemsTheSame(oldItem: DomainActivity, newItem: DomainActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DomainActivity, newItem: DomainActivity): Boolean {
            return oldItem == newItem
        }
    }
}