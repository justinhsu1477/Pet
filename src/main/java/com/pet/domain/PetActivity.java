package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pet_activity")
@Data
public class PetActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "walked")
    private Boolean walked = false;

    @Column(name = "walk_time")
    private LocalDateTime walkTime;

    @Column(name = "fed")
    private Boolean fed = false;

    @Column(name = "feed_time")
    private LocalDateTime feedTime;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
