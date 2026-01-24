package com.pet.service

import com.pet.domain.Booking
import com.pet.dto.response.*
import com.pet.repository.BookingRepository
import com.pet.repository.SitterRatingRepository
import com.pet.util.DateTimeUtils
import com.pet.util.DateTimeUtils.getEndOfCurrentMonth
import com.pet.util.DateTimeUtils.getEndOfCurrentWeek
import com.pet.util.DateTimeUtils.getRecentDates
import com.pet.util.DateTimeUtils.getStartOfCurrentMonth
import com.pet.util.DateTimeUtils.getStartOfCurrentWeek
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * 保母預約統計服務
 *
 * 提供以下統計功能：
 * 1. 預約統計（本月總數、待確認、已完成、拒絕/取消）
 * 2. 收入統計（本月收入、本週收入、每日趨勢）
 * 3. 評價統計（平均評分、五星比例、星級分布、最新評價）
 */
@Service
class BookingStatisticsService(
    private val bookingRepository: BookingRepository,
    private val sitterRatingRepository: SitterRatingRepository
) {

    /**
     * 取得保母的完整統計資料
     *
     * @param sitterId 保母 ID
     * @return 包含預約、收入、評價的完整統計
     */
    fun getStatistics(sitterId: UUID): BookingStatisticsResponse {
        return BookingStatisticsResponse(
            bookingStats = getBookingStats(sitterId),
            revenueStats = getRevenueStats(sitterId),
            ratingStats = getRatingStats(sitterId)
        )
    }

    /**
     * 計算本月的預約統計
     *
     * @param sitterId 保母 ID
     * @return 本月預約統計（總數、待確認、已完成、拒絕/取消）
     */
    fun getBookingStats(sitterId: UUID): BookingStats {
        val start = getStartOfCurrentMonth()
        val end = getEndOfCurrentMonth()

        // 查詢本月所有的預約
        val bookings = bookingRepository.findBySitterIdAndCreatedAtBetween(sitterId, start, end)

        val monthlyStats = MonthlyBookingStats(
            total = bookings.size.toLong(),
            pending = bookings.count { it.status == Booking.BookingStatus.PENDING }.toLong(),
            completed = bookings.count { it.status == Booking.BookingStatus.COMPLETED }.toLong(),
            rejectedOrCancelled = bookings.count {
                it.status == Booking.BookingStatus.REJECTED || it.status == Booking.BookingStatus.CANCELLED
            }.toLong()
        )

        return BookingStats(currentMonth = monthlyStats)
    }

    /**
     * 計算收入統計
     *
     * @param sitterId 保母 ID
     * @return 收入統計（本月收入、本週收入、每日趨勢）
     */
    fun getRevenueStats(sitterId: UUID): RevenueStats {
        val monthlyRevenue = bookingRepository.sumRevenueByCompletedBookings(
            sitterId,
            getStartOfCurrentMonth(),
            getEndOfCurrentMonth()
        ) ?: 0.0

        val weeklyRevenue = bookingRepository.sumRevenueByCompletedBookings(
            sitterId,
            getStartOfCurrentWeek(),
            getEndOfCurrentWeek()
        ) ?: 0.0

        // 優化每日趨勢查詢：
        // 1. 取得最近 7 天的日期列表
        val recentDates = getRecentDates(7)
        val startDate = DateTimeUtils.getStartOfDay(recentDates.first())
        val endDate = DateTimeUtils.getEndOfDay(recentDates.last())

        // 2. 一次性查詢這 7 天內所有的已完成預約，避免 N+1
        val recentBookings = bookingRepository.findBySitterIdAndCreatedAtBetween(sitterId, startDate, endDate)
            .filter { it.status == Booking.BookingStatus.COMPLETED }

        // 3. 按日期分組統計
        val bookingsByDate = recentBookings.groupBy { it.createdAt.toLocalDate() }

        val dailyTrend = recentDates.map { date ->
            val dayBookings = bookingsByDate[date] ?: emptyList()
            DailyRevenue(
                date = date,
                revenue = dayBookings.sumOf { it.totalPrice ?: 0.0 },
                bookingCount = dayBookings.size.toLong()
            )
        }

        return RevenueStats(
            monthlyRevenue = monthlyRevenue,
            weeklyRevenue = weeklyRevenue,
            dailyTrend = dailyTrend
        )
    }

    /**
     * 計算評價統計
     *
     * @param sitterId 保母 ID
     * @param limit 最新評價的數量限制（預設5筆）
     * @return 評價統計（平均分、五星比例、星級分布、最新評價）
     */
    fun getRatingStats(sitterId: UUID, limit: Int = 5): RatingStats {
        val averageRating = sitterRatingRepository.calculateAverageRating(sitterId) ?: 0.0
        val totalRatings = sitterRatingRepository.countBySitterId(sitterId)

        // 星級分佈
        val starCounts = sitterRatingRepository.countRatingsByStars(sitterId)
        val starDistribution = mutableMapOf<Int, Long>()
        (1..5).forEach { starDistribution[it] = 0L }
        starCounts.forEach {
            val star = it[0] as Int
            val count = it[1] as Long
            starDistribution[star] = count
        }

        // 五星比例
        val fiveStarCount = starDistribution[5] ?: 0L
        val fiveStarPercentage = if (totalRatings > 0) {
            (fiveStarCount.toDouble() / totalRatings) * 100
        } else {
            0.0
        }

        // 最新評價
        val latestRatingsRaw = sitterRatingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId)
        val latestRatings = latestRatingsRaw.take(limit).map { rating ->
            SimpleRatingDto(
                id = rating.id,
                overallRating = rating.overallRating,
                comment = rating.comment,
                createdAt = rating.createdAt,
                userName = if (rating.isAnonymous) null else rating.user?.username,
                isAnonymous = rating.isAnonymous
            )
        }

        return RatingStats(
            averageRating = averageRating,
            fiveStarPercentage = fiveStarPercentage,
            totalRatings = totalRatings,
            starDistribution = starDistribution,
            latestRatings = latestRatings
        )
    }

    // ============================================
    // 輔助方法（時間計算）- 委派給 DateTimeUtils
    // ============================================

}
