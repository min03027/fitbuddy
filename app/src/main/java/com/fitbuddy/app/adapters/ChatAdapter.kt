package com.fitbuddy.app.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout // FrameLayout 임포트
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fitbuddy.app.R
import com.fitbuddy.app.databinding.ItemMessageBinding
import com.fitbuddy.app.models.Message

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.tvMessage.text = message.text

            // FrameLayout의 LayoutParams를 사용합니다.
            val layoutParams = binding.messageContainer.layoutParams as FrameLayout.LayoutParams

            if (message.isUser) {
                // 사용자 메시지 (오른쪽)
                binding.messageContainer.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.indigo_600)
                )
                binding.tvMessage.setTextColor(
                    ContextCompat.getColor(itemView.context, android.R.color.white)
                )
                // layout_gravity를 사용하여 오른쪽으로 정렬합니다.
                layoutParams.gravity = Gravity.END
            } else {
                // AI 메시지 (왼쪽)
                binding.messageContainer.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, android.R.color.white)
                )
                binding.tvMessage.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.gray_900)
                )
                // layout_gravity를 사용하여 왼쪽으로 정렬합니다.
                layoutParams.gravity = Gravity.START
            }

            binding.messageContainer.layoutParams = layoutParams
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size
}
