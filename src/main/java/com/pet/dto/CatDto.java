package com.pet.dto;

import com.pet.domain.Cat;
import com.pet.domain.Pet;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CatDto(
        UUID id,

        @NotBlank(message = "寵物名稱不能為空")
        @Size(max = 100, message = "寵物名稱長度不能超過100個字元")
        String name,

        @Min(value = 0, message = "年齡不能為負數")
        @Max(value = 30, message = "貓咪年齡不能超過30歲")
        Integer age,

        @Size(max = 100, message = "品種長度不能超過100個字元")
        String breed,

        Pet.Gender gender,

        @Size(max = 500, message = "特殊需求長度不能超過500個字元")
        String specialNeeds,

        Boolean isNeutered,

        String vaccineStatus,

        // 貓特有屬性
        Boolean isIndoor,

        Cat.LitterBoxType litterBoxType,

        Cat.ScratchingHabit scratchingHabit
) {
}
