package com.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JWT 認證響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {

    /**
     * Access Token (短期,用於 API 請求)
     */
    private String accessToken;

    /**
     * Refresh Token (長期,用於刷新 Access Token)
     */
    private String refreshToken;

    /**
     * Token 類型
     */
    private String tokenType = "Bearer";

    /**
     * Access Token 過期時間 (秒)
     */
    private long expiresIn;

    /**
     * 用戶ID
     */
    private UUID userId;

    /**
     * 用戶名
     */
    private String username;

    /**
     * 角色
     */
    private String role;

    /**
     * 角色ID (Customer ID 或 Sitter ID)
     */
    private UUID roleId;

    /**
     * 角色名稱 (Customer 姓名或 Sitter 名稱)
     */
    private String roleName;

    /**
     * Email
     */
    private String email;

    /**
     * 電話
     */
    private String phone;
}
