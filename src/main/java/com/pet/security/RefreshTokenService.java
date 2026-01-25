package com.pet.security;

import com.pet.domain.RefreshToken;
import com.pet.domain.Users;
import com.pet.exception.BusinessException;
import com.pet.exception.ErrorCode;
import com.pet.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Refresh Token 管理服務
 * 職責:
 * 1. Token Hash 計算 (SHA-256)
 * 2. Token 的創建、驗證、撤銷
 * 3. 設備管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * 創建並保存 Refresh Token
     *
     * @param token 原始 JWT Token
     * @param user 用戶
     * @param deviceType 設備類型 (WEB/APP)
     * @param deviceInfo 設備信息 (可選)
     * @param ipAddress IP 地址 (可選)
     */
    @Transactional
    public RefreshToken createRefreshToken(
        String token,
        Users user,
        String deviceType,
        String deviceInfo,
        String ipAddress
    ) {
        // 計算 Token Hash
        String tokenHash = hashToken(token);

        // 撤銷該用戶在該設備上的舊 Token (同一設備只保留最新的 Token)
        refreshTokenRepository.revokeUserDeviceTokens(user.getId(), deviceType, true);

        // 創建新 Token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUser(user);
        refreshToken.setDeviceType(deviceType);
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setExpiryDate(
            LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000)
        );

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}, device: {}", user.getUsername(), deviceType);

        return saved;
    }

    /**
     * 驗證 Refresh Token
     *
     * @param token 原始 JWT Token
     * @return RefreshToken 實體
     * @throws BusinessException 如果 Token 無效
     */
    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        String tokenHash = hashToken(token);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh Token 不存在"));

        // 檢查是否已撤銷
        if (refreshToken.isRevoked()) {
            log.warn("Attempted to use revoked refresh token for user: {}",
                refreshToken.getUser().getUsername());
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh Token 已被撤銷");
        }

        // 檢查是否過期
        if (refreshToken.isExpired()) {
            log.warn("Attempted to use expired refresh token for user: {}",
                refreshToken.getUser().getUsername());
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh Token 已過期");
        }

        // 更新最後使用時間
        refreshToken.updateLastUsed();
        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    /**
     * 撤銷 Refresh Token
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        String tokenHash = hashToken(token);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("Revoked refresh token for user: {}", refreshToken.getUser().getUsername());
        });
    }

    /**
     * 撤銷用戶的所有 Token (用於登出所有設備)
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllUserTokens(userId, true);
        log.info("Revoked all refresh tokens for user: {}", userId);
    }

    /**
     * 撤銷用戶特定設備的 Token
     */
    @Transactional
    public void revokeUserDeviceTokens(UUID userId, String deviceType) {
        refreshTokenRepository.revokeUserDeviceTokens(userId, deviceType, true);
        log.info("Revoked {} refresh tokens for user: {}", deviceType, userId);
    }

    /**
     * 獲取用戶的所有有效 Token
     */
    public List<RefreshToken> getUserValidTokens(UUID userId) {
        return refreshTokenRepository.findValidTokensByUser(userId, false, LocalDateTime.now());
    }

    /**
     * 清理過期的 Token (定期任務調用)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired refresh tokens", deleted);
    }

    /**
     * 清理已撤銷且過期的 Token
     */
    @Transactional
    public void cleanupRevokedExpiredTokens() {
        int deleted = refreshTokenRepository.deleteRevokedExpiredTokens(true, LocalDateTime.now());
        log.info("Cleaned up {} revoked and expired refresh tokens", deleted);
    }

    /**
     * 統計用戶活躍設備數量
     */
    public long countActiveDevices(UUID userId) {
        return refreshTokenRepository.countActiveDevicesByUser(userId, false, LocalDateTime.now());
    }

    /**
     * 計算 Token 的 SHA-256 Hash
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            throw new RuntimeException("無法計算 Token Hash", e);
        }
    }
}
