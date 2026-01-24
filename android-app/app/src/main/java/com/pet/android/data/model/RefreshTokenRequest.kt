package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * Refresh Token 請求模型
 */
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)
