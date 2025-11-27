package com.fitbuddy.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ChatApiClient {

    private const val BASE_URL = "http://54.206.28.172:8000"

    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)   // 1분
            .readTimeout(300, TimeUnit.SECONDS)     // 5분
            .writeTimeout(300, TimeUnit.SECONDS)
            .callTimeout(300, TimeUnit.SECONDS)     // 전체 요청 5분
            .build()
    }

    val chatApi: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }
}
