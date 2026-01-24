package com.pet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新 Token 請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh Token 不能為空")
    private String refreshToken;

    /**
     * 設備類型 (可選,如果需要切換設備)
     */
    private String deviceType;
}
