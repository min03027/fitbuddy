package com.fitbuddy.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitbuddy.app.adapters.ChatAdapter
import com.fitbuddy.app.databinding.ActivityChatBinding
import com.fitbuddy.app.models.Message

class ChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupListeners()
        addInitialMessage()
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }
    
    private fun addInitialMessage() {
        messages.add(
            Message(
                text = "안녕하세요! 운동에 관해 궁금한 점을 물어보세요 😊",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
        chatAdapter.notifyDataSetChanged()
    }
    
    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        
        if (messageText.isEmpty()) return
        
        // 사용자 메시지 추가
        messages.add(
            Message(
                text = messageText,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
        )
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
        
        binding.etMessage.text.clear()
        
        // AI 응답 시뮬레이션 (1초 후)
        Handler(Looper.getMainLooper()).postDelayed({
            messages.add(
                Message(
                    text = "좋은 질문이네요! 운동은 꾸준함이 가장 중요합니다. 매일 조금씩이라도 운동하는 습관을 들이는 것을 추천드려요! 💪",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
            )
            chatAdapter.notifyItemInserted(messages.size - 1)
            binding.rvMessages.scrollToPosition(messages.size - 1)
        }, 1000)
    }
}
