package com.asis.virtualcompanion.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import com.asis.virtualcompanion.databinding.ItemMessageCompanionBinding
import com.asis.virtualcompanion.databinding.ItemMessageUserBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatMessageAdapter : ListAdapter<ChatMessageEntity, RecyclerView.ViewHolder>(ChatMessageDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFromUser) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_COMPANION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemMessageUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                UserMessageViewHolder(binding)
            }
            VIEW_TYPE_COMPANION -> {
                val binding = ItemMessageCompanionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                CompanionMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is CompanionMessageViewHolder -> holder.bind(message)
        }
    }

    inner class UserMessageViewHolder(
        private val binding: ItemMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun bind(message: ChatMessageEntity) {
            binding.messageText.text = message.message
            binding.timestampText.text = timeFormat.format(message.timestamp)
        }
    }

    inner class CompanionMessageViewHolder(
        private val binding: ItemMessageCompanionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun bind(message: ChatMessageEntity) {
            binding.messageText.text = message.message
            binding.timestampText.text = timeFormat.format(message.timestamp)
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_COMPANION = 2
    }
}

class ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessageEntity>() {
    override fun areItemsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
        return oldItem == newItem
    }
}
