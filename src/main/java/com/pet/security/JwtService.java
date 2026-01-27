package com.pet.security;

import com.pet.domain.UserRole;
import com.pet.domain.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT 服務類 - 負責生成和驗證 JWT Token
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("type", "ACCESS");

        return generateToken(claims, user.getUsername(), jwtProperties.getAccessTokenExpiration());
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("type", "REFRESH");

        return generateToken(claims, user.getUsername(), jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * 產生 LINE 註冊用臨時 Token
     */
    public String generateRegistrationToken(String lineUserId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("lineUserId", lineUserId);
        claims.put("type", "LINE_REGISTRATION");
        return generateToken(claims, "line-registration", 86400000L); // 24 hours
    }

    /**
     * 從註冊 Token 中提取 LINE User ID
     */
    public String extractLineUserIdFromRegistrationToken(String token) {
        Claims claims = extractClaims(token);
        String type = (String) claims.get("type");
        if (!"LINE_REGISTRATION".equals(type)) {
            throw new IllegalArgumentException("不是有效的註冊 Token");
        }
        return (String) claims.get("lineUserId");
    }

    /**
     * 生成 Token
     */
    private String generateToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtProperties.getIssuer())
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 從 Token 中提取用戶名
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 從 Token 中提取用戶 ID
     */
    public UUID extractUserId(String token) {
        String userId = (String) extractClaims(token).get("userId");
        return UUID.fromString(userId);
    }

    /**
     * 從 Token 中提取角色
     */
    public UserRole extractRole(String token) {
        String role = (String) extractClaims(token).get("role");
        return UserRole.valueOf(role);
    }

    /**
     * 從 Token 中提取權限
     */
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        String role = (String) extractClaims(token).get("role");
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    /**
     * 驗證 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 檢查 Token 是否過期
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * 從 Token 中提取所有聲明
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 獲取簽名密鑰
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 檢查是否為 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            String type = (String) extractClaims(token).get("type");
            return "REFRESH".equals(type);
        } catch (Exception e) {
            return false;
        }
    }
}