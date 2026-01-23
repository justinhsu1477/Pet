package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 保母視角的預約詳情 Response
 * 對應後端 BookingDto
 */
data class SitterBookingResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("petId")
    val petId: String,

    @SerializedName("petName")
    val petName: String,

    @SerializedName("sitterId")
    val sitterId: String,

    @SerializedName("sitterName")
    val sitterName: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String,

    @SerializedName("status")
    val status: BookingStatus,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("sitterResponse")
    val sitterResponse: String?,

    @SerializedName("totalPrice")
    val totalPrice: Double?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

/**
 * 預約狀態枚舉
 */
enum class BookingStatus {
    @SerializedName("PENDING")
    PENDING,      // 待確認

    @SerializedName("CONFIRMED")
    CONFIRMED,    // 已確認

    @SerializedName("REJECTED")
    REJECTED,     // 已拒絕

    @SerializedName("CANCELLED")
    CANCELLED,    // 已取消

    @SerializedName("COMPLETED")
    COMPLETED;    // 已完成

    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "待確認"
            CONFIRMED -> "已確認"
            REJECTED -> "已拒絕"
            CANCELLED -> "已取消"
            COMPLETED -> "已完成"
        }
    }

    fun getColorResId(): Int {
        return when (this) {
            PENDING -> android.R.color.holo_orange_dark
            CONFIRMED -> android.R.color.holo_green_dark
            REJECTED -> android.R.color.holo_red_dark
            CANCELLED -> android.R.color.darker_gray
            COMPLETED -> android.R.color.holo_blue_dark
        }
    }
}

/**
 * 確認預約請求
 */
data class ConfirmBookingRequest(
    @SerializedName("response")
    val response: String?
)

/**
 * 拒絕預約請求
 */
data class RejectBookingRequest(
    @SerializedName("reason")
    val reason: String?
)
