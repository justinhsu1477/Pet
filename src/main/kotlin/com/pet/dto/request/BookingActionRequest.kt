package com.pet.dto.request

import jakarta.validation.constraints.Size

/**
 * 保母確認/拒絕預約請求 DTO
 */
data class ConfirmBookingRequest(
    @field:Size(max = 500, message = "回覆內容不能超過500個字元")
    val response: String? = null
)

data class RejectBookingRequest(
    @field:Size(max = 500, message = "拒絕原因不能超過500個字元")
    val reason: String? = null
)
