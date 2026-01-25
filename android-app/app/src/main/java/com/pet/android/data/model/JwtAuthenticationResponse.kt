package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * JWT 認證響應模型
 * 包含 Access Token 和 Refresh Token
 *
 * 後端返回的是扁平結構，所有用戶資訊都在同一層級
 */
data class JwtAuthenticationResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String?,  // Web 端會從 Cookie 讀取，可能為 null

    @SerializedName("tokenType")
    val tokenType: String = "Bearer",

    @SerializedName("expiresIn")
    val expiresIn: Long? = null, // Access Token 過期時間（秒）

    // 用戶信息（扁平結構）
    @SerializedName("userId")
    val userId: String?,

    @SerializedName("username")
    val username: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("roleId")
    val roleId: String?,

    @SerializedName("roleName")
    val roleName: String?
) {
    /**
     * 提供一個方便的屬性來取得應該使用的 ID
     * 根據角色決定使用 userId 還是 roleId
     */
    val effectiveId: String?
        get() = when (role?.uppercase()) {
            "CUSTOMER" -> roleId  // Customer 使用 Customer.id (roleId)
            "SITTER" -> roleId    // Sitter 使用 Sitter.id (roleId)
            "ADMIN" -> userId     // Admin 使用 Users.id (userId)
            else -> userId        // 預設使用 userId
        }
}
