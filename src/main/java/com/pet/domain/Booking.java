package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 預約訂單
 */
@Entity
@Data
@Table(name = "booking",
        indexes = {
                @Index(name = "idx_booking_sitter_time", columnList = "sitter_id, start_time, end_time"),
                @Index(name = "idx_booking_status", columnList = "status")
        })
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sitter_id", nullable = false)
    private Sitter sitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    /**
     * 樂觀鎖版本號
     */
    @Version
    private Long version;

    /**
     * 預約備註（飼主留言）
     */
    @Column(length = 500)
    private String notes;

    /**
     * 保母回覆/拒絕原因
     */
    @Column(name = "sitter_response", length = 500)
    private String sitterResponse;

    /**
     * 服務費用
     */
    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 手動添加 Getter 以解決 Kotlin 訪問 Lombok 私有欄位的問題
     */
    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public UUID getId() {
        return id;
    }

    /**
     * 預約狀態枚舉
     * 狀態流轉：
     * PENDING → CONFIRMED (Sitter 接受)
     * PENDING → REJECTED (Sitter 拒絕)
     * PENDING → EXPIRED (超過24小時未回應，自動過期)
     * CONFIRMED → CANCELLED (飼主或 Sitter 取消)
     * CONFIRMED → COMPLETED (服務完成)
     */
    public enum BookingStatus {
        PENDING,      // 待確認
        CONFIRMED,    // 已確認
        REJECTED,     // 已拒絕
        CANCELLED,    // 已取消
        COMPLETED,    // 已完成
        EXPIRED       // 已過期（超過24小時未回應）
    }

    /**
     * 檢查是否可以轉換到目標狀態
     */
    public boolean canTransitionTo(BookingStatus targetStatus) {
        return switch (this.status) {
            case PENDING -> targetStatus == BookingStatus.CONFIRMED ||
                           targetStatus == BookingStatus.REJECTED ||
                           targetStatus == BookingStatus.CANCELLED ||
                           targetStatus == BookingStatus.EXPIRED;
            case CONFIRMED -> targetStatus == BookingStatus.CANCELLED ||
                             targetStatus == BookingStatus.COMPLETED;
            case REJECTED, CANCELLED, COMPLETED, EXPIRED -> false; // 終態，不可轉換
        };
    }
}
