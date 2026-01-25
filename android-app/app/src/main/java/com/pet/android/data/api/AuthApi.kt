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
     *
     * @deprecated 此 API 已棄用，請使用 jwtLogin()
     * 此 API 將在未來版本中移除
     *
     * 請使用新版 JWT API: jwtLogin(deviceType, request)
     */
    @Deprecated(
        message = "使用 jwtLogin() 代替，此方法將在未來版本中移除",
        replaceWith = ReplaceWith("jwtLogin(deviceType = \"APP\", request = request)"),
        level = DeprecationLevel.WARNING
    )
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
