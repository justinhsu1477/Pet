package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class SitterRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "sitter_id")
    private Sitter sitter;

    private LocalDateTime recordTime;
    private String activity;
    private Boolean fed;
    private Boolean walked;
    private String moodStatus;

    @Column(length = 1000)
    private String notes;

    @Column(length = 500)
    private String photos;

}
