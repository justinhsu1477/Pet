package com.pet.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SitterRecordDto(
        UUID id,
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
