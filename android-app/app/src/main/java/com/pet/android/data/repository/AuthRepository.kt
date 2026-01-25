package com.pet.android.data.repository

import android.util.Log
import com.pet.android.data.api.AuthApi
import com.pet.android.data.model.JwtAuthenticationResponse
import com.pet.android.data.model.LoginRequest
import com.pet.android.data.model.LoginResponse
import com.pet.android.data.model.RefreshTokenRequest
import com.pet.android.data.preferences.TokenManager
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userPreferencesManager: UserPreferencesManager,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * 舊版登入方法（保留向後兼容）
     *
     * @deprecated 此方法已棄用，請使用 jwtLogin()
     * 此方法將在未來版本中移除
     */
    @Deprecated(
        message = "使用 jwtLogin() 代替，此方法將在未來版本中移除",
        replaceWith = ReplaceWith("jwtLogin(username, password)"),
        level = DeprecationLevel.WARNING
    )
    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Attempting login for username: $username")

                // Check if account has changed and clear data if necessary
                val savedUsername = userPreferencesManager.username.first()
                if (savedUsername != null && savedUsername != username) {
                    Log.d(TAG, "Account changed from $savedUsername to $username, clearing old data")
                    userPreferencesManager.clearLoginData()
                }

                val response = authApi.login(LoginRequest(username, password))
                Log.d(TAG, "Login API response - success: ${response.success}, message: ${response.message}")
                Log.d(TAG, "Raw response data: ${response.data}")

                if (response.success && response.data != null) {
                    val loginData = response.data
                    Log.d(TAG, "Login successful - userId: ${loginData.userId}, roleId: ${loginData.roleId}, username: ${loginData.username}, role: ${loginData.role}")

                    // Get the appropriate ID based on role
                    val effectiveId = loginData.id
                    Log.d(TAG, "Effective ID for ${loginData.role}: $effectiveId")

                    // Check if id is null and warn
                    if (effectiveId == null) {
                        Log.w(TAG, "WARNING: Could not determine user ID! Using username as fallback")
                    }

                    // Save login data to DataStore
                    // Use id if available, otherwise fallback to username
                    val userIdToSave = effectiveId ?: loginData.username
                    Log.d(TAG, "Saving userId: $userIdToSave")

                    userPreferencesManager.saveLoginData(
                        username = loginData.username,
                        role = loginData.role,
                        userId = userIdToSave,
                        roleName = loginData.roleName
                    )

                    Log.d(TAG, "Login data saved to preferences")
                    Resource.Success(loginData)
                } else {
                    Log.e(TAG, "Login failed: ${response.message}")
                    Resource.Error(response.message ?: "登入失敗")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    /**
     * JWT 登入
     * 使用 JWT 認證機制進行登入，獲取 Access Token 和 Refresh Token
     */
    suspend fun jwtLogin(username: String, password: String): Resource<JwtAuthenticationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Attempting JWT login for username: $username")

                // Check if account has changed and clear data if necessary
                val savedUsername = userPreferencesManager.username.first()
                if (savedUsername != null && savedUsername != username) {
                    Log.d(TAG, "Account changed from $savedUsername to $username, clearing old data")
                    userPreferencesManager.clearLoginData()
                    tokenManager.clearTokens()
                }

                val response = authApi.jwtLogin(
                    deviceType = "APP",
                    request = LoginRequest(username, password)
                )

                Log.d(TAG, "JWT login API response - success: ${response.success}, message: ${response.message}")

                if (response.success && response.data != null) {
                    val jwtData = response.data

                    // 保存 Tokens (refreshToken 可能為 null，因為 Web 端使用 Cookie)
                    val refreshToken = jwtData.refreshToken
                    if (refreshToken != null) {
                        tokenManager.saveTokens(jwtData.accessToken, refreshToken)
                        Log.d(TAG, "JWT tokens saved successfully")
                    } else {
                        // 如果沒有 refreshToken (Web 端用 Cookie)，只保存 accessToken
                        tokenManager.saveAccessToken(jwtData.accessToken)
                        Log.d(TAG, "Access token saved successfully (no refresh token in response)")
                    }

                    // 保存用戶信息到 UserPreferences
                    if (jwtData.username != null && jwtData.role != null) {
                        val effectiveId = jwtData.effectiveId ?: jwtData.username
                        Log.d(TAG, "Saving user info - userId: ${jwtData.userId}, roleId: ${jwtData.roleId}, username: ${jwtData.username}, role: ${jwtData.role}")

                        userPreferencesManager.saveLoginData(
                            username = jwtData.username,
                            role = jwtData.role,
                            userId = effectiveId,
                            roleName = jwtData.roleName
                        )
                        Log.d(TAG, "User info saved to preferences")
                    } else {
                        Log.w(TAG, "User info incomplete in JWT response")
                    }

                    Resource.Success(jwtData)
                } else {
                    Log.e(TAG, "JWT login failed: ${response.message}")
                    Resource.Error(response.message ?: "登入失敗")
                }
            } catch (e: Exception) {
                Log.e(TAG, "JWT login error: ${e.message}", e)
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    /**
     * 刷新 Token
     * 使用 Refresh Token 獲取新的 Access Token
     */
    suspend fun refreshToken(): Resource<JwtAuthenticationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "No refresh token available")
                    return@withContext Resource.Error("未找到 Refresh Token")
                }

                Log.d(TAG, "Attempting to refresh token")
                val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))

                if (response.success && response.data != null) {
                    val jwtData = response.data

                    // 保存新的 Tokens
                    // 注意：後端可能只返回新的 accessToken，refreshToken 沿用舊的
                    if (jwtData.refreshToken != null) {
                        // 如果有新的 refreshToken，兩個都更新
                        tokenManager.saveTokens(jwtData.accessToken, jwtData.refreshToken)
                        Log.d(TAG, "Tokens refreshed and saved successfully (both tokens)")
                    } else {
                        // 如果沒有新的 refreshToken，只更新 accessToken
                        tokenManager.saveAccessToken(jwtData.accessToken)
                        Log.d(TAG, "Access token refreshed successfully (refresh token unchanged)")
                    }

                    Resource.Success(jwtData)
                } else {
                    Log.e(TAG, "Token refresh failed: ${response.message}")
                    Resource.Error(response.message ?: "刷新 Token 失敗")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh error: ${e.message}", e)
                Resource.Error(e.message ?: "網路錯誤")
            }
        }
    }

    /**
     * 登出
     * 清除本地 Tokens 並通知後端使 Refresh Token 失效
     */
    suspend fun logout(): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken()

                // 即使沒有 refresh token 也繼續清除本地數據
                if (!refreshToken.isNullOrEmpty()) {
                    try {
                        Log.d(TAG, "Attempting to logout on server")
                        val response = authApi.logout(RefreshTokenRequest(refreshToken))
                        Log.d(TAG, "Logout API response - success: ${response.success}")
                    } catch (e: Exception) {
                        // 即使後端登出失敗，也繼續清除本地數據
                        Log.e(TAG, "Server logout failed, but will clear local data anyway", e)
                    }
                }

                // 清除本地 Tokens 和用戶數據
                tokenManager.clearTokens()
                userPreferencesManager.clearLoginData()

                Log.d(TAG, "Local data cleared successfully")
                Resource.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Logout error: ${e.message}", e)
                // 即使出錯，也清除本地數據
                tokenManager.clearTokens()
                userPreferencesManager.clearLoginData()
                Resource.Error(e.message ?: "登出錯誤")
            }
        }
    }

    /**
     * 檢查是否已登入（有有效的 Token）
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.hasValidTokens()
    }

    /**
     * 檢查 Access Token 是否過期
     */
    fun isAccessTokenExpired(): Boolean {
        return tokenManager.isAccessTokenExpired()
    }
}
