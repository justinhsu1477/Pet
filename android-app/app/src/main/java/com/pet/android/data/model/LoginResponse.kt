package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("userId")
    val userId: String?, // admin 直接用 Users.id
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("role")
    val role: String,
    @SerializedName("roleId")
    val roleId: String?, // customer id or sitter id
    @SerializedName("roleName")
    val roleName: String?,
    @SerializedName("message")
    val message: String
) {
    // 提供一個方便的屬性來取得應該使用的 ID
    // 對於 SITTER，使用 roleId (Sitter.id) - 用於查詢保母相關數據
    // 對於 CUSTOMER 和 ADMIN，使用 userId (Users.id) - 用於查詢 Pet 等數據
    // 注意：Pet.user_id 指向 Users.id，所以 CUSTOMER 需要使用 userId
    val id: String?
        get() = when (role.uppercase()) {
            "SITTER" -> roleId  // Sitter 特殊情況，使用 Sitter.id
            else -> userId      // CUSTOMER 和 ADMIN 都使用 Users.id
        }
}
