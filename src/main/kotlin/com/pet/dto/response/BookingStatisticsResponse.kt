package com.pet.dto.response

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * 保母預約統計總覽響應
 */
data class BookingStatisticsResponse(
    val bookingStats: BookingStats,
    val revenueStats: RevenueStats,
    val ratingStats: RatingStats
)

/**
 * 預約統計
 */
data class BookingStats(
    val currentMonth: MonthlyBookingStats
)

/**
 * 月度預約統計
 */
data class MonthlyBookingStats(
    /** 總預約數 */
    val total: Long,

    /** 待確認數量 (PENDING) */
    val pending: Long,

    /** 已完成數量 (COMPLETED) */
    val completed: Long,

    /** 拒絕/取消數量 (REJECTED + CANCELLED) */
    val rejectedOrCancelled: Long
)

/**
 * 收入統計
 */
data class RevenueStats(
    /** 本月總收入（自然月，只計算 COMPLETED 訂單） */
    val monthlyRevenue: Double,

    /** 本週收入（自然週，週一到週日） */
    val weeklyRevenue: Double,

    /** 每日收入趨勢（最近7天） */
    val dailyTrend: List<DailyRevenue>
)

/**
 * 每日收入明細
 */
data class DailyRevenue(
    /** 日期 */
    val date: LocalDate,

    /** 當日收入 */
    val revenue: Double,

    /** 當日預約數 */
    val bookingCount: Long
)

/**
 * 評價統計
 */
data class RatingStats(
    /** 平均評分 */
    val averageRating: Double,

    /** 五星比例（五星評價數 / 總評價數） */
    val fiveStarPercentage: Double,

    /** 總評價數 */
    val totalRatings: Long,

    /** 各星級分布（1-5星的數量） */
    val starDistribution: Map<Int, Long>,

    /** 最新評價列表（預設5筆） */
    val latestRatings: List<SimpleRatingDto>
)

/**
 * 簡化的評價資料（用於列表展示）
 */
data class SimpleRatingDto(
    val id: UUID,
    val overallRating: Int,
    val comment: String?,
    val createdAt: LocalDateTime,
    val userName: String?,  // 如果是匿名評價則為 null
    val isAnonymous: Boolean
)