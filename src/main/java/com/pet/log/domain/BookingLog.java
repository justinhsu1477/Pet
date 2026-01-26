package com.pet.log.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Booking Log Entity (扁平化存儲)
 * 用於 Log DB ，專門用於報表/分析
 * 不包含 @ManyToOne 關聯，直接存儲關聯實體的 ID 和名稱
 */
@Entity
@Data
@Table(name = "booking_log",
        indexes = {
                @Index(name = "idx_booking_log_booking_id", columnList = "booking_id")
        })
public class BookingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "pet_id")
    private UUID petId;

    @Column(name = "pet_name", length = 100)
    private String petName;

    @Column(name = "sitter_id")
    private UUID sitterId;

    @Column(name = "sitter_name", length = 100)
    private String sitterName;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 20)
    private String status;

    @Column(length = 500)
    private String notes;

    @Column(name = "sitter_response", length = 500)
    private String sitterResponse;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "booking_created_at")
    private LocalDateTime bookingCreatedAt;

    @Column(name = "booking_updated_at")
    private LocalDateTime bookingUpdatedAt;

    @Column(name = "sync_time", nullable = false)
    private LocalDateTime syncTime;

    @PrePersist
    protected void onCreate() {
        if (syncTime == null) {
            syncTime = LocalDateTime.now();
        }
    }
}
