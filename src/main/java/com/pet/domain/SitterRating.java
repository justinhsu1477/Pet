package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 保母評價
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sitter_id", nullable = false)
    private Sitter sitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /**
     * 總體評分 (1-5)
     */
    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    /**
     * 專業度評分 (1-5)
     */
    @Column(name = "professionalism_rating")
    private Integer professionalismRating;

    /**
     * 溝通評分 (1-5)
     */
    @Column(name = "communication_rating")
    private Integer communicationRating;

    /**
     * 準時性評分 (1-5)
     */
    @Column(name = "punctuality_rating")
    private Integer punctualityRating;

    @Column(length = 1000)
    private String comment;

    /**
     * 保母回覆
     */
    @Column(name = "sitter_reply", length = 500)
    private String sitterReply;

    /**
     * 是否匿名評價
     */
    @Column(name = "is_anonymous")
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

    // --- 以下為 Getter/Setter 重寫（保留原本設計，解決特定環境讀取問題） ---

    public UUID getId() { return id; }
    public Integer getOverallRating() { return overallRating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getIsAnonymous() { return isAnonymous; }
    public Users getUser() { return user; }

    /**
     * 計算加權平均分數
     */
    public Double getWeightedScore() {
        double score = overallRating * 0.4;
        score += (professionalismRating != null ? professionalismRating : overallRating) * 0.25;
        score += (communicationRating != null ? communicationRating : overallRating) * 0.20;
        score += (punctualityRating != null ? punctualityRating : overallRating) * 0.15;
        return Math.round(score * 100.0) / 100.0;
    }
}