package com.pet.dto;

import com.pet.domain.ExperienceLevel;
import java.util.UUID;

/**
 * 可用保母資訊 DTO（用於預約頁面顯示）
 */
public record AvailableSitterDto(
        UUID id,
        String name,
        String experience,
        Double averageRating,
        Integer ratingCount,
        Integer completedBookings,
        Double hourlyRate,
        ExperienceLevel experienceLevel
) {
}
