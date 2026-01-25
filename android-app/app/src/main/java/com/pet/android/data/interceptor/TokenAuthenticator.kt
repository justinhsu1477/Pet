package com.pet.android.data.interceptor

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pet.android.data.api.AuthApi
import com.pet.android.data.model.RefreshTokenRequest
import com.pet.android.data.preferences.TokenManager
import com.pet.android.data.preferences.UserPreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token 認證器
 * 當收到 401 Unauthorized 時自動刷新 Token 並重試請求
 * 使用 synchronized 避免並發刷新
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
    private val userPreferencesManager: UserPreferencesManager
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val MAX_RETRY_COUNT = 3
    }

    // 使用鎖避免並發刷新
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "Authentication required for: ${response.request.url}")

        // 檢查重試次數，避免無限循環
        val retryCount = response.request.header("X-Retry-Count")?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRY_COUNT) {
            Log.e(TAG, "Max retry count reached, giving up")
            handleLogout()
            return null
        }

        // 獲取 Refresh Token
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "No refresh token available, cannot refresh")
            handleLogout()
            return null
        }

        // 使用 synchronized 避免並發刷新
        synchronized(lock) {
            // 再次檢查 Token（可能已被其他線程刷新）
            val currentToken = tokenManager.getAccessToken()
            val requestToken = response.request.header(HEADER_AUTHORIZATION)
                ?.removePrefix("Bearer ")

            // 如果 Token 已經更新（與請求中的不同），使用新 Token 重試
            if (currentToken != null && currentToken != requestToken) {
                Log.d(TAG, "Token already refreshed by another thread, using new token")
                return response.request.newBuilder()
                    .header(HEADER_AUTHORIZATION, "Bearer $currentToken")
                    .header("X-Retry-Count", (retryCount + 1).toString())
                    .build()
            }

            // 執行 Token 刷新
            return try {
                Log.d(TAG, "Attempting to refresh token")
                val newTokens = runBlocking {
                    refreshTokenSync(refreshToken)
                }

                if (newTokens != null) {
                    Log.d(TAG, "Token refreshed successfully")
                    // 使用新 Token 重試請求
                    response.request.newBuilder()
                        .header(HEADER_AUTHORIZATION, "Bearer ${newTokens.first}")
                        .header("X-Retry-Count", (retryCount + 1).toString())
                        .build()
                } else {
                    Log.e(TAG, "Token refresh failed")
                    handleLogout()
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during token refresh", e)
                handleLogout()
                null
            }
        }
    }

    /**
     * 同步刷新 Token
     * @return Pair<accessToken, refreshToken> 或 null
     */
    private suspend fun refreshTokenSync(refreshToken: String): Pair<String, String>? {
        return try {
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))

            if (response.success && response.data != null) {
                val newAccessToken = response.data.accessToken
                val newRefreshToken = response.data.refreshToken

                // 保存新的 Tokens
                if (newRefreshToken != null) {
                    // 如果有新的 refreshToken，兩個都更新
                    tokenManager.saveTokens(newAccessToken, newRefreshToken)
                    Log.d(TAG, "New tokens saved (both tokens)")
                } else {
                    // 如果沒有新的 refreshToken，只更新 accessToken（沿用舊的 refreshToken）
                    tokenManager.saveAccessToken(newAccessToken)
                    Log.d(TAG, "New access token saved (refresh token unchanged)")
                }

                // 返回新的 accessToken 和 refreshToken（可能是舊的）
                Pair(newAccessToken, newRefreshToken ?: refreshToken)
            } else {
                Log.e(TAG, "Token refresh API returned unsuccessful response: ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during token refresh API call", e)
            null
        }
    }

    /**
     * 處理登出邏輯
     * 清除 Tokens 和用戶數據，跳轉到登入頁面
     */
    private fun handleLogout() {
        Log.d(TAG, "Handling logout - clearing tokens and user data")

        // 清除 Tokens
        tokenManager.clearTokens()

        // 清除用戶數據
        runBlocking {
            userPreferencesManager.clearLoginData()
        }

        // 跳轉到登入頁面
        // 使用 FLAG_ACTIVITY_NEW_TASK 和 FLAG_ACTIVITY_CLEAR_TASK 清除返回堆疊
        try {
            val loginIntent = Intent().apply {
                setClassName(context, "com.pet.android.ui.login.LoginActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(loginIntent)
            Log.d(TAG, "Navigated to login screen")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to login screen", e)
        }
    }
}
