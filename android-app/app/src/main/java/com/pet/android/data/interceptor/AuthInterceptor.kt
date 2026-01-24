package com.pet.android.data.interceptor

import android.util.Log
import com.pet.android.data.preferences.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 認證攔截器
 * 自動在請求中添加 Authorization Header 和 X-Device-Type Header
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_DEVICE_TYPE = "X-Device-Type"
        private const val DEVICE_TYPE_APP = "APP"
        private const val TOKEN_TYPE_BEARER = "Bearer"

        // 不需要添加 Token 的端點
        private val EXCLUDED_PATHS = listOf(
            "/api/auth/login",
            "/api/auth/jwt/login",
            "/api/auth/jwt/refresh",
            "/api/auth/register"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // 檢查是否為排除的端點
        val isExcluded = EXCLUDED_PATHS.any { path.contains(it) }

        // 如果是排除的端點，不添加 Token
        if (isExcluded) {
            Log.d(TAG, "Skipping token injection for path: $path")
            // 但仍然添加 X-Device-Type header
            val request = originalRequest.newBuilder()
                .addHeader(HEADER_DEVICE_TYPE, DEVICE_TYPE_APP)
                .build()
            return chain.proceed(request)
        }

        // 獲取 Access Token
        val accessToken = tokenManager.getAccessToken()

        // 如果沒有 Token，直接發送請求（會觸發 401）
        if (accessToken.isNullOrEmpty()) {
            Log.d(TAG, "No access token found, proceeding without Authorization header")
            val request = originalRequest.newBuilder()
                .addHeader(HEADER_DEVICE_TYPE, DEVICE_TYPE_APP)
                .build()
            return chain.proceed(request)
        }

        // 添加 Authorization Header 和 X-Device-Type Header
        val request = originalRequest.newBuilder()
            .addHeader(HEADER_AUTHORIZATION, "$TOKEN_TYPE_BEARER $accessToken")
            .addHeader(HEADER_DEVICE_TYPE, DEVICE_TYPE_APP)
            .build()

        Log.d(TAG, "Added Authorization header for path: $path")

        return chain.proceed(request)
    }
}
