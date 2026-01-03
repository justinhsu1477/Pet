package com.pet.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record PetDto(
        UUID id,

        @NotBlank(message = "寵物名稱不能為空") @Size(max = 100, message = "寵物名稱長度不能超過100個字元") String name,

        @NotBlank(message = "寵物類型不能為空") @Size(max = 50, message = "寵物類型長度不能超過50個字元") String type,

        @Min(value = 0, message = "年齡不能為負數") @Max(value = 50, message = "年齡不能超過50歲") Integer age,

        @Size(max = 100, message = "品種長度不能超過100個字元") String breed,

        @NotBlank(message = "飼主姓名不能為空") @Size(max = 100, message = "飼主姓名長度不能超過100個字元") String ownerName,

        @NotBlank(message = "飼主電話不能為空") @Pattern(regexp = "^[0-9-+()\\s]*$", message = "電話格式不正確") @Size(max = 20, message = "電話長度不能超過20個字元") String ownerPhone,

        @Size(max = 500, message = "特殊需求長度不能超過500個字元") String specialNeeds) {
}
