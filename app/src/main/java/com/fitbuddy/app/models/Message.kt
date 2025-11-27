package com.fitbuddy.app.models

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
