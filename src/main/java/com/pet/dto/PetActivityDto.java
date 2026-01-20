package com.pet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PetActivityDto(
        UUID id,

        @NotNull(message = "寵物ID不能為空")
        UUID petId,

        @NotNull(message = "活動日期不能為空")
        LocalDate activityDate,

        Boolean walked,

        LocalDateTime walkTime,

        Boolean fed,

        LocalDateTime feedTime,

        @Size(max = 500, message = "備註長度不能超過500個字元")
        String notes,

        LocalDateTime createdAt
) {
}
