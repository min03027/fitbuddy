package com.fitbuddy.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitbuddy.app.adapters.ChatAdapter
import com.fitbuddy.app.databinding.ActivityChatBinding
import com.fitbuddy.app.models.Message
import com.fitbuddy.app.network.ChatApiClient
import com.fitbuddy.app.network.ChatRequest

import kotlinx.coroutines.launch

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
                text = "안녕하세요! 운동에 대해 무엇이든 물어보세요 😊",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
        chatAdapter.notifyDataSetChanged()
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) return

        // 1) 사용자 메시지 UI 추가
        addUserMessage(messageText)
        binding.etMessage.text.clear()

        // 2) 백엔드 API 호출
        lifecycleScope.launch {
            try {
                val response = ChatApiClient.chatApi.sendMessage(ChatRequest(messageText))



                if (response.isSuccessful) {
                    val reply = response.body()?.reply ?: "응답을 받을 수 없어요 😢"
                    addBotMessage(reply)
                } else {
                    addBotMessage("서버 오류가 발생했습니다. (${response.code()})")
                }

            } catch (e: Exception) {
                e.printStackTrace()  // Logcat에 전체 스택 출력
                addBotMessage("연결 실패: ${e.javaClass.simpleName} - ${e.message}")
            }


        }
    }

    private fun addUserMessage(text: String) {
        messages.add(
            Message(
                text = text,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
        )
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(
            Message(
                text = text,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }
}
