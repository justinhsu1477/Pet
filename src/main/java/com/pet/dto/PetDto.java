package com.pet.dto;

import com.pet.domain.Pet;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record PetDto(
        UUID id,

        @NotBlank(message = "寵物名稱不能為空")
        @Size(max = 100, message = "寵物名稱長度不能超過100個字元")
        String name,

        @Min(value = 0, message = "年齡不能為負數")
        @Max(value = 50, message = "年齡不能超過50歲")
        Integer age,

        @Size(max = 100, message = "品種長度不能超過100個字元")
        String breed,

        Pet.Gender gender,

        @Size(max = 500, message = "特殊需求長度不能超過500個字元")
        String specialNeeds,

        Boolean isNeutered,

        String vaccineStatus,

        // 寵物類型 (CAT/DOG)
        String petType,

        // 寵物類型名稱 (貓/狗)
        String petTypeName
) {
}
