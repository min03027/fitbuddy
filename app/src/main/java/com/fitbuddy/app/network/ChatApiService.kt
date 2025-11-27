package com.fitbuddy.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {

    @Headers("Content-Type: application/json")
    @POST("/api/chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
