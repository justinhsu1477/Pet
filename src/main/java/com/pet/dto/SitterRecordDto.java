package com.pet.dto;

import java.time.LocalDateTime;

public record SitterRecordDto(
        Long id,
        PetDto pet,
        SitterDto sitter,
        LocalDateTime recordTime,
        String activity,
        Boolean fed,
        Boolean walked,
        String moodStatus,
        String notes,
        String photos) {
}
