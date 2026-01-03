package com.pet.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "用戶名不能為空")
        String username,

        @NotBlank(message = "密碼不能為空")
        String password
) {
}