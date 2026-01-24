package com.pet.android.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pet.android.data.preferences.TokenManager
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登出助手
 * 提供全局登出功能，清除所有認證數據並跳轉到登入頁面
 */
@Singleton
class LogoutHelper @Inject constructor(
    private val tokenManager: TokenManager,
    private val userPreferencesManager: UserPreferencesManager
) {
    companion object {
        private const val TAG = "LogoutHelper"
    }

    /**
     * 執行登出
     * 清除所有認證數據並跳轉到登入頁面
     */
    fun performLogout(context: Context, showToast: Boolean = true) {
        Log.d(TAG, "Performing logout")

        // 清除 Tokens
        tokenManager.clearTokens()

        // 清除用戶數據
        CoroutineScope(Dispatchers.IO).launch {
            userPreferencesManager.clearLoginData()
        }

        // 跳轉到登入頁面
        navigateToLogin(context, showToast)
    }

    /**
     * 跳轉到登入頁面
     */
    private fun navigateToLogin(context: Context, showToast: Boolean) {
        try {
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (showToast) {
                    putExtra("SHOW_LOGOUT_MESSAGE", true)
                }
            }
            context.startActivity(intent)
            Log.d(TAG, "Navigated to login screen")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to login screen", e)
        }
    }

    /**
     * 處理未授權錯誤（401）
     * Token 過期或無效時調用
     */
    fun handleUnauthorized(context: Context) {
        Log.w(TAG, "Handling unauthorized access - clearing tokens and redirecting to login")
        performLogout(context, showToast = false)
    }
}
