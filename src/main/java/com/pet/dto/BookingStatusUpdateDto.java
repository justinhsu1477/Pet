package com.pet.dto;

import com.pet.domain.Booking.BookingStatus;
import jakarta.validation.constraints.*;

/**
 * 預約狀態更新 DTO
 * 用於 Sitter 接受/拒絕預約，或取消/完成訂單
 */
public record BookingStatusUpdateDto(
        @NotNull(message = "目標狀態不能為空")
        BookingStatus targetStatus,

        @Size(max = 500, message = "回覆/原因長度不能超過500個字元")
        String reason
) {
}
