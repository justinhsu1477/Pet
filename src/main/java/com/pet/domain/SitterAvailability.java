package com.pet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
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

    // Explicit accessors for Kotlin interop (Lombok not visible to Kotlin compiler)
    public UUID getId() { return id; }
    public Sitter getSitter() { return sitter; }
    public void setSitter(Sitter sitter) { this.sitter = sitter; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
