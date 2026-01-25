package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 保母可接案時段
 */
@Entity
@Data
@Table(name = "sitter_availability",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"sitter_id", "day_of_week", "start_time", "end_time"}
        ))
public class SitterAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sitter_id", nullable = false)
    private Sitter sitter;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * 服務地區（可擴展為獨立表）
     */
    @Column(length = 100, name = "service_area")
    private String serviceArea;

    /**
     * 是否啟用此時段
     */
    @Column(name = "is_active")
    private Boolean isActive = true;
}
