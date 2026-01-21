package com.pet.dto;

import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record SitterAvailabilityDto(
        UUID id,

        @NotNull(message = "保母 ID 不能為空")
        UUID sitterId,

        @NotNull(message = "星期幾不能為空")
        DayOfWeek dayOfWeek,

        @NotNull(message = "開始時間不能為空")
        LocalTime startTime,

        @NotNull(message = "結束時間不能為空")
        LocalTime endTime,

        @Size(max = 100, message = "服務地區長度不能超過100個字元")
        String serviceArea,

        Boolean isActive
) {
}
