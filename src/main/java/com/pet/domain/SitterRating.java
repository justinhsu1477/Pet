package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 保母評價
 * 1. 限制只有 COMPLETED 訂單才能評分（防濫用）
 * 2. 一個 Booking 只能評價一次（唯一索引）
 * 3. 支援加權平均計算
 */
@Entity
@Data
@Table(name = "sitter_rating",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_rating",
                columnNames = {"booking_id"}
        ))
public class SitterRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 關聯的預約訂單（必須是 COMPLETED 狀態）
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * 被評價的保母
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sitter_id", nullable = false)
    private Sitter sitter;

    /**
     * 評價者（飼主）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /**
     * 總體評分 (1-5)
     */
    @Column(nullable = false)
    private Integer overallRating;

    /**
     * 專業度評分 (1-5)
     */
    private Integer professionalismRating;

    /**
     * 溝通評分 (1-5)
     */
    private Integer communicationRating;

    /**
     * 準時性評分 (1-5)
     */
    private Integer punctualityRating;

    /**
     * 評價內容
     */
    @Column(length = 1000)
    private String comment;

    /**
     * 保母回覆
     */
    @Column(length = 500)
    private String sitterReply;

    /**
     * 是否匿名評價
     */
    private Boolean isAnonymous = false;

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

    public UUID getId() {
        return id;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public Users getUser() {
        return user;
    }

    /**
     * 計算加權平均分數
     * 權重：總體 40%, 專業 25%, 溝通 20%, 準時 15%
     */
    public Double getWeightedScore() {
        double score = overallRating * 0.4;
        if (professionalismRating != null) {
            score += professionalismRating * 0.25;
        } else {
            score += overallRating * 0.25;
        }
        if (communicationRating != null) {
            score += communicationRating * 0.20;
        } else {
            score += overallRating * 0.20;
        }
        if (punctualityRating != null) {
            score += punctualityRating * 0.15;
        } else {
            score += overallRating * 0.15;
        }
        return Math.round(score * 100.0) / 100.0;
    }
}
