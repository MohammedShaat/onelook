package com.example.onelook.ui.home

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.onelook.R
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.data.domain.TodayTask
import com.example.onelook.databinding.ItemActivityHistoryBinding
import com.example.onelook.databinding.ItemSupplementHistoryBinding

class TodayTasksAdapter(private val resource: Resources) :
    ListAdapter<TodayTask, ViewHolder>(TodayTaskDiffUtil()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is SupplementHistoryViewHolder -> holder.bind(getItem(position) as SupplementHistory)
            is ActivityHistoryViewHolder -> holder.bind(getItem(position) as ActivityHistory)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SUPPLEMENT_HISTORY -> {
                val binding = ItemSupplementHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SupplementHistoryViewHolder(binding)
            }
            else -> {
                val binding = ItemActivityHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ActivityHistoryViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SupplementHistory -> VIEW_TYPE_SUPPLEMENT_HISTORY
            else -> VIEW_TYPE_ACTIVITY_HISTORY
        }
    }

    companion object {
        const val VIEW_TYPE_SUPPLEMENT_HISTORY = 0
        const val VIEW_TYPE_ACTIVITY_HISTORY = 1
    }

    inner class SupplementHistoryViewHolder(private val binding: ItemSupplementHistoryBinding) :
        ViewHolder(binding.root) {

        fun bind(supplementHistory: SupplementHistory) {
            binding.apply {
                textViewTaskName.text = supplementHistory.name
                imageViewSupplementIcon.supplementHistoryImage(supplementHistory.formattedForm)
                textViewTakingWithMeals.text =
                    resource.getString(
                        R.string.item_supplement_history_text_view_task_name,
                        supplementHistory.dosage,
                        supplementHistory.form,
                        supplementHistory.takingWithMeals
                    )
                customSupplementProgressView.total = supplementHistory.dosage
                customSupplementProgressView.progress = supplementHistory.progress
            }
        }
    }

    inner class ActivityHistoryViewHolder(private val binding: ItemActivityHistoryBinding) :
        ViewHolder(binding.root) {

        fun bind(activityHistory: ActivityHistory) {
            binding.apply {
                textViewTaskName.text = activityHistory.type.replaceFirstChar { it.uppercase() }
                imageViewActivityIcon.activityHistoryImage(activityHistory.formattedType)
                customActivityProgressView.activityHistoryProgress(
                    activityHistory.formattedProgress,
                    activityHistory.formattedDuration
                )
                buttonContinueExercise.isVisible = !activityHistory.completed
            }
        }
    }

    class TodayTaskDiffUtil : DiffUtil.ItemCallback<TodayTask>() {
        override fun areItemsTheSame(oldItem: TodayTask, newItem: TodayTask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TodayTask, newItem: TodayTask): Boolean {
            return when (oldItem) {
                is SupplementHistory -> (oldItem as SupplementHistory) == (newItem as SupplementHistory)
                else -> (oldItem as ActivityHistory) == (newItem as ActivityHistory)
            }
        }
    }
}