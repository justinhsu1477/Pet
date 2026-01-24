package com.pet.android.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token Manager
 * 使用 EncryptedSharedPreferences 安全存儲 JWT Token
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_FILE_NAME = "encrypted_token_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val EXPIRY_BUFFER_SECONDS = 60 // 提前60秒認為 Token 過期
    }

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            // 創建 MasterKey
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // 創建 EncryptedSharedPreferences
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating EncryptedSharedPreferences, falling back to regular SharedPreferences", e)
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * 保存 Access Token
     */
    fun saveAccessToken(token: String) {
        try {
            sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
            Log.d(TAG, "Access token saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving access token", e)
        }
    }

    /**
     * 保存 Refresh Token
     */
    fun saveRefreshToken(token: String) {
        try {
            sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
            Log.d(TAG, "Refresh token saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving refresh token", e)
        }
    }

    /**
     * 同時保存 Access Token 和 Refresh Token
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        try {
            sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply()
            Log.d(TAG, "Tokens saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving tokens", e)
        }
    }

    /**
     * 獲取 Access Token
     */
    fun getAccessToken(): String? {
        return try {
            sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting access token", e)
            null
        }
    }

    /**
     * 獲取 Refresh Token
     */
    fun getRefreshToken(): String? {
        return try {
            sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting refresh token", e)
            null
        }
    }

    /**
     * 清除所有 Token
     */
    fun clearTokens() {
        try {
            sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply()
            Log.d(TAG, "Tokens cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tokens", e)
        }
    }

    /**
     * 檢查 Access Token 是否過期
     * 使用 Auth0 JWT 庫解析 Token
     */
    fun isAccessTokenExpired(): Boolean {
        val token = getAccessToken() ?: return true

        return try {
            val jwt = JWT.decode(token)
            val expiresAt = jwt.expiresAt ?: return true

            // 提前 EXPIRY_BUFFER_SECONDS 秒認為已過期，預留刷新時間
            val bufferTime = Date(System.currentTimeMillis() + EXPIRY_BUFFER_SECONDS * 1000)
            val isExpired = expiresAt.before(bufferTime)

            Log.d(TAG, "Token expiry check - expires at: $expiresAt, is expired: $isExpired")
            isExpired
        } catch (e: JWTDecodeException) {
            Log.e(TAG, "Error decoding JWT token", e)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error checking token expiry", e)
            true
        }
    }

    /**
     * 檢查是否有有效的 Tokens
     */
    fun hasValidTokens(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null && !isAccessTokenExpired()
    }

    /**
     * 獲取 Token 的過期時間（Unix timestamp in seconds）
     */
    fun getTokenExpiryTime(): Long? {
        val token = getAccessToken() ?: return null

        return try {
            val jwt = JWT.decode(token)
            jwt.expiresAt?.time?.div(1000) // Convert to seconds
        } catch (e: Exception) {
            Log.e(TAG, "Error getting token expiry time", e)
            null
        }
    }

    /**
     * 從 Token 中提取用戶信息（可選）
     */
    fun getUserIdFromToken(): String? {
        val token = getAccessToken() ?: return null

        return try {
            val jwt = JWT.decode(token)
            jwt.getClaim("userId").asString()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting user ID from token", e)
            null
        }
    }

    /**
     * 從 Token 中提取用戶角色（可選）
     */
    fun getUserRoleFromToken(): String? {
        val token = getAccessToken() ?: return null

        return try {
            val jwt = JWT.decode(token)
            jwt.getClaim("role").asString()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting user role from token", e)
            null
        }
    }
}
