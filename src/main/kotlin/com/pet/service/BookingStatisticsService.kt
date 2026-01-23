package com.pet.service

import com.pet.dto.response.*
import com.pet.repository.BookingRepository
import com.pet.repository.SitterRatingRepository
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
        // TODO: 組合所有統計資料並返回
        // 提示：呼叫 getBookingStats、getRevenueStats、getRatingStats 三個方法
        TODO("實作：組合三種統計資料")
    }

    /**
     * 計算本月的預約統計
     *
     * @param sitterId 保母 ID
     * @return 本月預約統計（總數、待確認、已完成、拒絕/取消）
     */
    fun getBookingStats(sitterId: UUID): BookingStats {
        // TODO: 計算本月的預約統計
        // 提示：
        // 1. 取得本月的起始和結束時間（自然月）
        //    - 起始：本月1號 00:00:00
        //    - 結束：本月最後一天 23:59:59
        // 2. 使用 bookingRepository.findBySitterIdAndCreatedAtBetween() 查詢本月的預約
        // 3. 按狀態分組計數：
        //    - total: 所有預約總數
        //    - pending: status = PENDING 的數量
        //    - completed: status = COMPLETED 的數量
        //    - rejectedOrCancelled: status = REJECTED 或 CANCELLED 的數量
        // 4. 組裝成 MonthlyBookingStats 並包裝在 BookingStats 中返回
        //
        // 實作方式可選擇：
        // 方法 1: 查詢所有預約，然後在記憶體中用 filter/count 統計（適合資料量小）
        // 方法 2: 使用多個 count 查詢分別統計（適合資料量大）
        TODO("實作：計算本月預約統計")
    }

    /**
     * 計算收入統計
     *
     * @param sitterId 保母 ID
     * @return 收入統計（本月收入、本週收入、每日趨勢）
     */
    fun getRevenueStats(sitterId: UUID): RevenueStats {
        // TODO: 計算收入統計
        // 提示：
        // 1. 計算本月收入：
        //    - 時間範圍：本月1號 00:00:00 到本月最後一天 23:59:59
        //    - 使用 bookingRepository.sumRevenueByCompletedBookings()
        //    - 只計算 status = COMPLETED 的訂單
        //
        // 2. 計算本週收入：
        //    - 時間範圍：本週週一 00:00:00 到本週週日 23:59:59
        //    - 使用 bookingRepository.sumRevenueByCompletedBookings()
        //
        // 3. 計算每日收入趨勢（最近7天）：
        //    - 取得最近7天的日期列表（包含今天）
        //    - 對每一天：
        //      a. 使用 bookingRepository.findCompletedBookingsByDate() 查詢該日的已完成預約
        //      b. 計算該日總收入：bookings.sumOf { it.totalPrice ?: 0.0 }
        //      c. 計算該日預約數：bookings.size
        //      d. 組裝成 DailyRevenue
        //    - 組合成 List<DailyRevenue>
        //
        // 4. 組裝成 RevenueStats 返回
        //
        // 注意：如果查詢結果為 null，要使用預設值 0.0 或 emptyList()
        TODO("實作：計算收入統計")
    }

    /**
     * 計算評價統計
     *
     * @param sitterId 保母 ID
     * @param limit 最新評價的數量限制（預設5筆）
     * @return 評價統計（平均分、五星比例、星級分布、最新評價）
     */
    fun getRatingStats(sitterId: UUID, limit: Int = 5): RatingStats {
        // TODO: 計算評價統計
        // 提示：
        // 1. 取得平均評分：
        //    - 使用 sitterRatingRepository.calculateAverageRating(sitterId)
        //    - 如果沒有評價，預設為 0.0
        //
        // 2. 取得總評價數：
        //    - 使用 sitterRatingRepository.countBySitterId(sitterId)
        //
        // 3. 取得星級分布（1-5星的數量）：
        //    - 使用 sitterRatingRepository.countRatingsByStars(sitterId)
        //    - 返回格式是 List<Array<Any>>，例如 [[5, 10], [4, 3], ...]
        //    - 需要轉換成 Map<Int, Long>，例如 {5=10, 4=3, ...}
        //    - 確保 1-5 星都有資料，沒有評價的星級要補 0
        //      提示：(1..5).forEach { star -> if (!map.containsKey(star)) map[star] = 0L }
        //
        // 4. 計算五星比例：
        //    - 五星數量 = starDistribution[5] ?: 0
        //    - 五星比例 = (五星數量 / 總評價數) * 100
        //    - 如果沒有評價，比例為 0.0
        //
        // 5. 取得最新評價：
        //    - 使用 sitterRatingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId)
        //    - 取前 limit 筆
        //    - 轉換成 SimpleRatingDto：
        //      a. 如果 isAnonymous = true，userName 要設為 null
        //      b. 如果 isAnonymous = false，userName = rating.user.username
        //
        // 6. 組裝成 RatingStats 返回
        TODO("實作：計算評價統計")
    }

    // ============================================
    // 輔助方法（時間計算）
    // ============================================

    /**
     * 取得本月的起始時間
     * @return 本月1號 00:00:00
     */
    private fun getStartOfCurrentMonth(): LocalDateTime {
        // TODO: 實作取得本月起始時間
        // 提示：
        // return LocalDate.now()
        //     .withDayOfMonth(1)  // 月初第1天
        //     .atStartOfDay()      // 00:00:00
        TODO("實作：取得本月起始時間")
    }

    /**
     * 取得本月的結束時間
     * @return 本月最後一天 23:59:59.999999999
     */
    private fun getEndOfCurrentMonth(): LocalDateTime {
        // TODO: 實作取得本月結束時間
        // 提示：
        // return LocalDate.now()
        //     .with(TemporalAdjusters.lastDayOfMonth())  // 月底最後一天
        //     .atTime(23, 59, 59, 999_999_999)           // 23:59:59.999999999
        TODO("實作：取得本月結束時間")
    }

    /**
     * 取得本週的起始時間
     * @return 本週週一 00:00:00
     */
    private fun getStartOfCurrentWeek(): LocalDateTime {
        // TODO: 實作取得本週起始時間
        // 提示：
        // return LocalDate.now()
        //     .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))  // 本週週一
        //     .atStartOfDay()
        TODO("實作：取得本週起始時間")
    }

    /**
     * 取得本週的結束時間
     * @return 本週週日 23:59:59.999999999
     */
    private fun getEndOfCurrentWeek(): LocalDateTime {
        // TODO: 實作取得本週結束時間
        // 提示：
        // return LocalDate.now()
        //     .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))  // 本週週日
        //     .atTime(23, 59, 59, 999_999_999)
        TODO("實作：取得本週結束時間")
    }

    /**
     * 取得最近 N 天的日期列表（包含今天）
     * @param days 天數
     * @return 日期列表，從最舊到最新排序
     *
     * 例如：days = 7 會返回 [7天前, 6天前, ..., 昨天, 今天]
     */
    private fun getRecentDates(days: Int): List<LocalDate> {
        // TODO: 實作取得最近 N 天的日期列表
        // 提示：
        // val today = LocalDate.now()
        // return (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
        TODO("實作：取得最近 N 天的日期列表")
    }
}
