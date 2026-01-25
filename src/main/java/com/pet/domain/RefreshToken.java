package com.pet.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token 實體 - 用於存儲和管理 Refresh Token
 *
 * 設計考量:
 * 1. 存儲 Token Hash 而非原文 (更安全,類似密碼存儲)
 * 2. 支援設備類型追蹤 (WEB/APP)
 * 3. 記錄 IP 和設備信息 (用於異常檢測)
 * 4. 添加索引優化查詢效能
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_token_hash", columnList = "tokenHash"),
    @Index(name = "idx_user_device", columnList = "user_id,deviceType"),
    @Index(name = "idx_expiry", columnList = "expiryDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Token 的 SHA256 Hash (不存原文,更安全)
     */
    @Column(nullable = false, unique = true, length = 64, name = "token_hash")
    private String tokenHash;

    /**
     * 關聯的用戶
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /**
     * 過期時間
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 是否已撤銷
     */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * 設備類型: WEB/APP
     */
    @Column(nullable = false, length = 20)
    private String deviceType;

    /**
     * 設備信息 (可選): "Chrome 120 on Windows", "iOS 17.2"
     */
    @Column(length = 200)
    private String deviceInfo;

    /**
     * 登入 IP (可選,用於異常檢測)
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * 最後使用時間
     */
    private LocalDateTime lastUsedAt;

    /**
     * 創建時間
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }

    /**
     * 檢查 Token 是否過期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 檢查 Token 是否有效
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }

    /**
     * 更新最後使用時間
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
