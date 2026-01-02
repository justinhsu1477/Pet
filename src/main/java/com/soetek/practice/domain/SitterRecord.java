package com.soetek.practice.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SitterRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Sitter getSitter() {
        return sitter;
    }

    public void setSitter(Sitter sitter) {
        this.sitter = sitter;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Boolean getFed() {
        return fed;
    }

    public void setFed(Boolean fed) {
        this.fed = fed;
    }

    public Boolean getWalked() {
        return walked;
    }

    public void setWalked(Boolean walked) {
        this.walked = walked;
    }

    public String getMoodStatus() {
        return moodStatus;
    }

    public void setMoodStatus(String moodStatus) {
        this.moodStatus = moodStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }
}
