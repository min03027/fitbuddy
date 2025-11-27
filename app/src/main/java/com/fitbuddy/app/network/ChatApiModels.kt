package com.fitbuddy.app.network

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val reply: String
)
