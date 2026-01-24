package com.pet.dto;

import com.pet.domain.Booking.BookingStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 預約訂單 DTO
 */
public record BookingDto(
        UUID id,

        @NotNull(message = "寵物 ID 不能為空")
        UUID petId,

        String petName,

        @NotNull(message = "保母 ID 不能為空")
        UUID sitterId,

        String sitterName,

        UUID userId,

        String username,

        @NotNull(message = "開始時間不能為空")
        @FutureOrPresent(message = "開始時間不能是過去時間")
        LocalDateTime startTime,

        @NotNull(message = "結束時間不能為空")
        @Future(message = "結束時間必須是未來時間")
        LocalDateTime endTime,

        BookingStatus status,

        @Size(max = 500, message = "備註長度不能超過500個字元")
        String notes,

        String sitterResponse,

        @PositiveOrZero(message = "費用不能為負數")
        Double totalPrice,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
    /**
     * 建立請求用的簡化建構子
     */
    public static BookingDto createRequest(UUID petId, UUID sitterId,
            LocalDateTime startTime, LocalDateTime endTime, String notes) {
        return new BookingDto(null, petId, null, sitterId, null, null, null,
                startTime, endTime, null, notes, null, null, null, null);
    }
}
