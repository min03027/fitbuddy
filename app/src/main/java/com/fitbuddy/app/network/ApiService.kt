package com.fitbuddy.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// --- 요청/응답 데이터 클래스 ---

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String
)

data class SignupResponse(
    val success: Boolean,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String
)

data class UserInfoRequest(
    val email: String,
    val height_cm: Int,
    val weight_kg: Double,
    val gender: String,
    val workout_goal: String
)

data class UserInfoResponse(
    val success: Boolean,
    val message: String
)

interface ApiService {

    @GET("/")
    suspend fun ping(): Map<String, String>

    @POST("/signup")
    suspend fun signup(@Body body: SignupRequest): SignupResponse

    @POST("/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("/user/info")
    suspend fun updateUserInfo(
        @Body request: UserInfoRequest
    ): UserInfoResponse

    // 🔥 **이 부분이 없으면 오류 발생**
    @POST("/pose/analyze")
    suspend fun analyzePose(@Body body: PoseImageRequest): PoseImageResponse
}

data class PoseImageRequest(
    val image_base64: String
)

data class PoseImageResponse(
    val knee_angle: Float,
    val hip_angle: Float,
    val torso_tilt: Float,
    val feedback: String
)

