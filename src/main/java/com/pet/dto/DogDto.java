package com.pet.dto;

import com.pet.domain.Dog;
import com.pet.domain.Pet;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record DogDto(
        UUID id,

        @NotBlank(message = "寵物名稱不能為空")
        @Size(max = 100, message = "寵物名稱長度不能超過100個字元")
        String name,

        @Min(value = 0, message = "年齡不能為負數")
        @Max(value = 25, message = "狗狗年齡不能超過25歲")
        Integer age,

        @Size(max = 100, message = "品種長度不能超過100個字元")
        String breed,

        Pet.Gender gender,

        @Size(max = 500, message = "特殊需求長度不能超過500個字元")
        String specialNeeds,

        Boolean isNeutered,

        String vaccineStatus,

        // 狗特有屬性
        Dog.Size size,

        Boolean isWalkRequired,

        @Min(value = 0, message = "遛狗次數不能為負數")
        @Max(value = 10, message = "每日遛狗次數不能超過10次")
        Integer walkFrequencyPerDay,

        Dog.TrainingLevel trainingLevel,

        Boolean isFriendlyWithDogs,

        Boolean isFriendlyWithPeople,

        Boolean isFriendlyWithChildren
) {
}
