package com.pet.dto;

import jakarta.validation.constraints.*;

public record SitterDto(
        Long id,

        @NotBlank(message = "保母姓名不能為空") @Size(max = 100, message = "保母姓名長度不能超過100個字元") String name,

        @NotBlank(message = "電話不能為空") @Pattern(regexp = "^[0-9-+()\\s]*$", message = "電話格式不正確") @Size(max = 20, message = "電話長度不能超過20個字元") String phone,

        @Email(message = "Email 格式不正確") @Size(max = 100, message = "Email 長度不能超過100個字元") String email,

        @Size(max = 500, message = "經驗描述長度不能超過500個字元") String experience) {
}
