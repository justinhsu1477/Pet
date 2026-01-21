package com.pet.dto;

import java.util.UUID;

/**
 * 保母評價統計 DTO
 * 用於顯示保母的總體評價概況
 */
public record SitterRatingStatsDto(
        UUID sitterId,
        String sitterName,
        Double averageRating,
        Double averageProfessionalism,
        Double averageCommunication,
        Double averagePunctuality,
        Integer totalRatings,
        Integer completedBookings,

        // 評分分佈
        Integer fiveStarCount,
        Integer fourStarCount,
        Integer threeStarCount,
        Integer twoStarCount,
        Integer oneStarCount
) {
}
