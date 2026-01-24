package com.pet.android.util

/**
 * 認證相關常量
 */
object AuthConstants {
    // Header 名稱
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_DEVICE_TYPE = "X-Device-Type"

    // Token 類型
    const val TOKEN_TYPE_BEARER = "Bearer"

    // 設備類型
    const val DEVICE_TYPE_APP = "APP"
    const val DEVICE_TYPE_WEB = "WEB"
    const val DEVICE_TYPE_IOS = "IOS"

    // Token 過期緩衝時間（秒）
    const val TOKEN_EXPIRY_BUFFER_SECONDS = 60

    // 重試次數
    const val MAX_TOKEN_REFRESH_RETRY = 3

    // Intent Extra Keys
    const val EXTRA_SHOW_LOGOUT_MESSAGE = "SHOW_LOGOUT_MESSAGE"
    const val EXTRA_SESSION_EXPIRED = "SESSION_EXPIRED"

    // API 端點
    object Endpoints {
        const val LOGIN = "api/auth/login"
        const val JWT_LOGIN = "api/auth/jwt/login"
        const val JWT_REFRESH = "api/auth/jwt/refresh"
        const val JWT_LOGOUT = "api/auth/jwt/logout"
        const val REGISTER = "api/auth/register"
    }

    // 錯誤訊息
    object ErrorMessages {
        const val NO_REFRESH_TOKEN = "未找到 Refresh Token"
        const val TOKEN_REFRESH_FAILED = "刷新 Token 失敗"
        const val LOGIN_FAILED = "登入失敗"
        const val LOGOUT_FAILED = "登出錯誤"
        const val NETWORK_ERROR = "網路錯誤"
        const val INVALID_CREDENTIALS = "請輸入帳號和密碼"
        const val SESSION_EXPIRED = "登入已過期，請重新登入"
    }

    // 成功訊息
    object SuccessMessages {
        const val LOGIN_SUCCESS = "登入成功"
        const val LOGOUT_SUCCESS = "登出成功"
        const val AUTO_LOGIN = "自動登入中..."
    }
}
