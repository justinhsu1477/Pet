package com.pet.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 寵物照片
 * 支援 LINE 上傳和網頁上傳兩種來源
 */
@Entity
@Table(name = "pet_photo",
        indexes = {
                @Index(name = "idx_pet_photo_pet", columnList = "pet_id"),
                @Index(name = "idx_pet_photo_sitter", columnList = "sitter_id"),
                @Index(name = "idx_pet_photo_uploaded_at", columnList = "uploaded_at")
        })
@Getter
@Setter
public class PetPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 關聯的寵物（可為 null，當無法自動判斷時）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    /**
     * 上傳的保母
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sitter_id", nullable = false)
    private Sitter sitter;

    /**
     * 照片儲存路徑（相對路徑）
     */
    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    /**
     * LINE Message ID（用於防止重複處理）
     */
    @Column(name = "message_id", length = 100)
    private String messageId;

    /**
     * 上傳來源
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "upload_source", nullable = false, length = 20)
    private UploadSource uploadSource = UploadSource.LINE;

    /**
     * 照片說明
     */
    @Column(name = "caption", length = 500)
    private String caption;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public enum UploadSource {
        LINE, WEB
    }

    // Explicit getters/setters for Kotlin interop
    public UUID getId() { return id; }
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    public Sitter getSitter() { return sitter; }
    public void setSitter(Sitter sitter) { this.sitter = sitter; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public UploadSource getUploadSource() { return uploadSource; }
    public void setUploadSource(UploadSource uploadSource) { this.uploadSource = uploadSource; }
    public String getCaption() { return caption; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
