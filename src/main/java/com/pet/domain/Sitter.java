package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Entity
@Data
public class Sitter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 關聯到 Users 帳號
     * 一對一關聯，一個 Sitter 對應一個 Users
     */
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    private Users user;

    /**
     * 保母顯示名稱/服務名稱
     */
    @Column(nullable = false)
    private String name;

    /**
     * 服務經驗描述
     */
    @Column(length = 500)
    private String experience;

    /**
     * 平均評分（反正規化，用於快速排序查詢）
     * 每次新增評價時更新
     */
    @Column(name = "average_rating")
    private Double averageRating;

    /**
     * 評價總數
     */
    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    /**
     * 完成訂單數
     */
    @Column(name = "completed_bookings")
    private Integer completedBookings = 0;
}
