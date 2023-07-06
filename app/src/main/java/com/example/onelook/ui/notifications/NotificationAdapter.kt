package com.example.onelook.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.Notification
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.databinding.ItemNotificationBinding
import com.example.onelook.ui.home.activityIcon
import com.example.onelook.ui.home.supplementIcon
import com.example.onelook.util.parseDate
import com.github.marlonlom.utilities.timeago.TimeAgo

class NotificationAdapter(private val onClickListener: (Notification) -> Unit) :
    ListAdapter<Notification, NotificationAdapter.NotificationVH>(NotificationDiff()) {

    override fun onBindViewHolder(holder: NotificationVH, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationVH {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationVH(binding)
    }

    inner class NotificationVH(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onClickListener(getItem(adapterPosition))
            }
        }

        fun bind(notification: Notification) {
            binding.apply {
                (notification.history as? SupplementHistory)?.let { imageViewIcon.supplementIcon(it.parsedForm) }
                    ?: imageViewIcon.activityIcon((notification.history as ActivityHistory).parsedType)
                textViewMessage.text = notification.message
                textViewTime.text = TimeAgo.using(notification.createdAt.parseDate.time)
            }
        }
    }

    class NotificationDiff : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}