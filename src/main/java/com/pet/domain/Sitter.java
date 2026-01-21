package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Sitter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String phone;
    private String email;

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
