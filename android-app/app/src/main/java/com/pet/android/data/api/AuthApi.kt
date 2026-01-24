package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.JwtAuthenticationResponse
import com.pet.android.data.model.LoginRequest
import com.pet.android.data.model.LoginResponse
import com.pet.android.data.model.RefreshTokenRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    /**
     * 舊版登入 API (保持向後兼容)
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    /**
     * JWT 登入 API
     * @param deviceType 設備類型，預設為 "APP"
     * @param request 登入請求（用戶名和密碼）
     * @return JWT 認證響應，包含 accessToken 和 refreshToken
     */
    @POST("api/auth/jwt/login")
    suspend fun jwtLogin(
        @Header("X-Device-Type") deviceType: String = "APP",
        @Body request: LoginRequest
    ): ApiResponse<JwtAuthenticationResponse>

    /**
     * 刷新 Token API
     * @param request Refresh Token 請求
     * @return 新的 JWT 認證響應
     */
    @POST("api/auth/jwt/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<JwtAuthenticationResponse>

    /**
     * 登出 API
     * @param request Refresh Token 請求（用於使 Token 失效）
     * @return 登出結果
     */
    @POST("api/auth/jwt/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest
    ): ApiResponse<Void>
}
