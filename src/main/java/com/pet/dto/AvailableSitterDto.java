package com.pet.dto;

import java.util.UUID;

/**
 * 可用保母資訊 DTO（用於預約頁面顯示）
 */
public record AvailableSitterDto(
        UUID id,
        String name,
        String phone,
        String email,
        String experience,
        Double averageRating,
        Integer ratingCount,
        Integer completedBookings
) {
}
