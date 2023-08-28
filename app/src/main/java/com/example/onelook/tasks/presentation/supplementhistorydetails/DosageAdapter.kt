package com.example.onelook.tasks.presentation.supplementhistorydetails

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.onelook.R
import com.example.onelook.databinding.ItemDosageBinding

class DosageAdapter(private val onCheckedChangeListener: (Boolean, Dosage) -> Unit) :
    ListAdapter<Dosage, DosageAdapter.DosageVH>(DosageDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DosageVH {
        val binding = ItemDosageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DosageVH(binding)
    }

    override fun onBindViewHolder(holder: DosageVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DosageVH(private val binding: ItemDosageBinding) : ViewHolder(binding.root) {

        init {
            binding.checkBoxDosageName.setOnCheckedChangeListener { _, isChecked ->
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener
                onCheckedChangeListener(isChecked, getItem(adapterPosition))
            }
        }

        fun bind(dosage: Dosage) {
            binding.apply {
                checkBoxDosageName.apply {
                    isChecked = dosage.isChecked
                    text = resources.getString(R.string.dosage_name, dosage.id + 1)
                }
                textViewDosageTime.text = dosage.time
            }
        }
    }

    class DosageDiff : DiffUtil.ItemCallback<Dosage>() {
        override fun areItemsTheSame(oldItem: Dosage, newItem: Dosage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Dosage, newItem: Dosage): Boolean {
            return oldItem == newItem
        }
    }
}