package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * JWT 認證響應模型
 * 包含 Access Token 和 Refresh Token
 */
data class JwtAuthenticationResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("tokenType")
    val tokenType: String = "Bearer",

    @SerializedName("expiresIn")
    val expiresIn: Long? = null, // Access Token 過期時間（秒）

    @SerializedName("userInfo")
    val userInfo: UserInfo? = null
) {
    /**
     * 用戶信息
     */
    data class UserInfo(
        @SerializedName("userId")
        val userId: String?,

        @SerializedName("username")
        val username: String,

        @SerializedName("email")
        val email: String?,

        @SerializedName("phone")
        val phone: String?,

        @SerializedName("role")
        val role: String,

        @SerializedName("roleId")
        val roleId: String?,

        @SerializedName("roleName")
        val roleName: String?
    ) {
        // 提供一個方便的屬性來取得應該使用的 ID
        val id: String?
            get() = when (role.uppercase()) {
                "SITTER" -> roleId  // Sitter 特殊情況，使用 Sitter.id
                else -> userId      // CUSTOMER 和 ADMIN 都使用 Users.id
            }
    }
}
