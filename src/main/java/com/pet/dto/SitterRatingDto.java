package com.pet.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 保母評價 DTO
 */
public record SitterRatingDto(
        UUID id,

        @NotNull(message = "預約訂單 ID 不能為空")
        UUID bookingId,

        UUID sitterId,

        String sitterName,

        UUID userId,

        String userName,

        @NotNull(message = "總體評分不能為空")
        @Min(value = 1, message = "評分最低為1")
        @Max(value = 5, message = "評分最高為5")
        Integer overallRating,

        @Min(value = 1, message = "評分最低為1")
        @Max(value = 5, message = "評分最高為5")
        Integer professionalismRating,

        @Min(value = 1, message = "評分最低為1")
        @Max(value = 5, message = "評分最高為5")
        Integer communicationRating,

        @Min(value = 1, message = "評分最低為1")
        @Max(value = 5, message = "評分最高為5")
        Integer punctualityRating,

        @Size(max = 1000, message = "評價內容長度不能超過1000個字元")
        String comment,

        String sitterReply,

        Boolean isAnonymous,

        Double weightedScore,

        LocalDateTime createdAt
) {
    /**
     * 建立評價請求用的簡化建構子
     */
    public static SitterRatingDto createRequest(UUID bookingId, Integer overallRating,
            Integer professionalismRating, Integer communicationRating,
            Integer punctualityRating, String comment, Boolean isAnonymous) {
        return new SitterRatingDto(null, bookingId, null, null, null, null,
                overallRating, professionalismRating, communicationRating,
                punctualityRating, comment, null, isAnonymous, null, null);
    }
}
