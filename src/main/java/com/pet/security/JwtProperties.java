package com.pet.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT 簽名密鑰
     */
    private String secret = "your-256-bit-secret-key-change-this-in-production-environment-please-use-strong-secret";

    /**
     * Access Token 有效期 (毫秒) - 默認 15 分鐘
     */
    private long accessTokenExpiration = 900000; // 15 minutes

    /**
     * Refresh Token 有效期 (毫秒) - 默認 7 天
     */
    private long refreshTokenExpiration = 604800000; // 7 days

    /**
     * Token 發行者
     */
    private String issuer = "pet-care-system";
}