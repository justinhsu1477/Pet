package com.pet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserDto(
        UUID id,

        @NotBlank(message = "用戶名不能為空")
        @Size(max = 50, message = "用戶名長度不能超過50個字元")
        String username,

        @Email(message = "Email 格式不正確")
        @Size(max = 100, message = "Email 長度不能超過100個字元")
        String email,

        @Size(max = 20, message = "電話長度不能超過20個字元")
        String phone,

        String role
) {
}
