package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 一般用戶（飼主）詳細資料
 */
@Entity
@Data
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 關聯到 Users 帳號
     * 一對一關聯，一個 Customer 對應一個 Users
     */
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @ToString.Exclude
    private Users user;

    /**
     * 用戶真實姓名
     */
    @Column(nullable = false)
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 緊急聯絡人
     */
    @Column(name = "emergency_contact")
    private String emergencyContact;

    /**
     * 緊急聯絡人電話
     */
    @Column(name = "emergency_phone")
    private String emergencyPhone;

    /**
     * 會員等級 (可選,例如: BRONZE, SILVER, GOLD)
     */
    @Column(name = "member_level")
    private String memberLevel = "BRONZE";

    /**
     * 累計預約次數
     */
    @Column(name = "total_bookings")
    private Integer totalBookings = 0;

    /**
     * 累計消費金額
     */
    @Column(name = "total_spent")
    private Double totalSpent = 0.0;

    /**
     * 建立時間
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
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

    // ==========================================
    // 手動添加 Getter 以解決 Kotlin 訪問 Lombok 私有欄位的問題
    // ==========================================

    public UUID getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public Integer getTotalBookings() {
        return totalBookings;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
