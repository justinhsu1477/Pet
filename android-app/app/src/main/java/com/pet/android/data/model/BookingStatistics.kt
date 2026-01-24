package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 保母預約統計總覽響應
 */
data class BookingStatisticsResponse(
    @SerializedName("bookingStats")
    val bookingStats: BookingStats,

    @SerializedName("revenueStats")
    val revenueStats: RevenueStats,

    @SerializedName("ratingStats")
    val ratingStats: RatingStats
)

/**
 * 預約統計
 */
data class BookingStats(
    @SerializedName("currentMonth")
    val currentMonth: MonthlyBookingStats
)

/**
 * 月度預約統計
 */
data class MonthlyBookingStats(
    /** 總預約數 */
    @SerializedName("total")
    val total: Long,

    /** 待確認數量 (PENDING) */
    @SerializedName("pending")
    val pending: Long,

    /** 已完成數量 (COMPLETED) */
    @SerializedName("completed")
    val completed: Long,

    /** 拒絕/取消數量 (REJECTED + CANCELLED) */
    @SerializedName("rejectedOrCancelled")
    val rejectedOrCancelled: Long
)

/**
 * 收入統計
 */
data class RevenueStats(
    /** 本月總收入（自然月，只計算 COMPLETED 訂單） */
    @SerializedName("monthlyRevenue")
    val monthlyRevenue: Double,

    /** 本週收入（自然週，週一到週日） */
    @SerializedName("weeklyRevenue")
    val weeklyRevenue: Double,

    /** 每日收入趨勢（最近7天） */
    @SerializedName("dailyTrend")
    val dailyTrend: List<DailyRevenue>
)

/**
 * 每日收入明細
 */
data class DailyRevenue(
    /** 日期 */
    @SerializedName("date")
    val date: String,  // LocalDate 序列化為字串

    /** 當日收入 */
    @SerializedName("revenue")
    val revenue: Double,

    /** 當日預約數 */
    @SerializedName("bookingCount")
    val bookingCount: Long
)

/**
 * 評價統計
 */
data class RatingStats(
    /** 平均評分 */
    @SerializedName("averageRating")
    val averageRating: Double,

    /** 五星比例（五星評價數 / 總評價數） */
    @SerializedName("fiveStarPercentage")
    val fiveStarPercentage: Double,

    /** 總評價數 */
    @SerializedName("totalRatings")
    val totalRatings: Long,

    /** 各星級分布（1-5星的數量） */
    @SerializedName("starDistribution")
    val starDistribution: Map<String, Long>,

    /** 最新評價列表（預設5筆） */
    @SerializedName("latestRatings")
    val latestRatings: List<SimpleRating>
)

/**
 * 簡化的評價資料（用於列表展示）
 */
data class SimpleRating(
    @SerializedName("id")
    val id: String,

    @SerializedName("overallRating")
    val overallRating: Int,

    @SerializedName("comment")
    val comment: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("userName")
    val userName: String?,  // 如果是匿名評價則為 null

    @SerializedName("isAnonymous")
    val isAnonymous: Boolean
)
