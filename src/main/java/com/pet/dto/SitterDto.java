package com.pet.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record SitterDto(
        UUID id,

        @NotBlank(message = "保母姓名不能為空") @Size(max = 100, message = "保母姓名長度不能超過100個字元") String name,

        @Size(max = 500, message = "經驗描述長度不能超過500個字元") String experience) {
}
