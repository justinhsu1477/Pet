package com.pet.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

/**
 * 日期時間工具類
 *
 * 提供常用的日期時間計算方法，統一管理日期時間相關的邏輯
 */
object DateTimeUtils {

    // ============================================
    // 月份相關方法
    // ============================================

    /**
     * 取得本月的起始時間
     * @return 本月1號 00:00:00
     *
     * 範例：2026-01-24 15:30:00 → 2026-01-01 00:00:00
     */
    @JvmStatic
    fun getStartOfCurrentMonth(): LocalDateTime {
        return LocalDate.now()
            .withDayOfMonth(1)  // 月初第1天
            .atStartOfDay()      // 00:00:00
    }

    /**
     * 取得本月的結束時間
     * @return 本月最後一天 23:59:59.999999999
     *
     * 範例：2026-01-24 15:30:00 → 2026-01-31 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfCurrentMonth(): LocalDateTime {
        return LocalDate.now()
            .with(TemporalAdjusters.lastDayOfMonth())  // 月底最後一天
            .atTime(23, 59, 59, 999_999_999)           // 23:59:59.999999999
    }

    /**
     * 取得指定日期所在月份的起始時間
     * @param date 指定日期
     * @return 該月1號 00:00:00
     */
    @JvmStatic
    fun getStartOfMonth(date: LocalDate): LocalDateTime {
        return date
            .withDayOfMonth(1)
            .atStartOfDay()
    }

    /**
     * 取得指定日期所在月份的結束時間
     * @param date 指定日期
     * @return 該月最後一天 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfMonth(date: LocalDate): LocalDateTime {
        return date
            .with(TemporalAdjusters.lastDayOfMonth())
            .atTime(23, 59, 59, 999_999_999)
    }

    // ============================================
    // 週相關方法
    // ============================================

    /**
     * 取得本週的起始時間（週一 00:00:00）
     * @return 本週週一 00:00:00
     *
     * 範例：2026-01-24 (五) 15:30:00 → 2026-01-20 (一) 00:00:00
     */
    @JvmStatic
    fun getStartOfCurrentWeek(): LocalDateTime {
        return LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))  // 本週週一
            .atStartOfDay()
    }

    /**
     * 取得本週的結束時間（週日 23:59:59）
     * @return 本週週日 23:59:59.999999999
     *
     * 範例：2026-01-24 (五) 15:30:00 → 2026-01-26 (日) 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfCurrentWeek(): LocalDateTime {
        return LocalDate.now()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))  // 本週週日
            .atTime(23, 59, 59, 999_999_999)
    }

    /**
     * 取得指定日期所在週的起始時間
     * @param date 指定日期
     * @return 該週週一 00:00:00
     */
    @JvmStatic
    fun getStartOfWeek(date: LocalDate): LocalDateTime {
        return date
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay()
    }

    /**
     * 取得指定日期所在週的結束時間
     * @param date 指定日期
     * @return 該週週日 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfWeek(date: LocalDate): LocalDateTime {
        return date
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .atTime(23, 59, 59, 999_999_999)
    }

    // ============================================
    // 日期相關方法
    // ============================================

    /**
     * 取得某一天的起始時間
     * @param date 指定日期
     * @return 該日 00:00:00
     */
    @JvmStatic
    fun getStartOfDay(date: LocalDate): LocalDateTime {
        return date.atStartOfDay()
    }

    /**
     * 取得某一天的結束時間
     * @param date 指定日期
     * @return 該日 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfDay(date: LocalDate): LocalDateTime {
        return date.atTime(23, 59, 59, 999_999_999)
    }

    /**
     * 取得今天的起始時間
     * @return 今天 00:00:00
     */
    @JvmStatic
    fun getStartOfToday(): LocalDateTime {
        return LocalDate.now().atStartOfDay()
    }

    /**
     * 取得今天的結束時間
     * @return 今天 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfToday(): LocalDateTime {
        return LocalDate.now().atTime(23, 59, 59, 999_999_999)
    }

    // ============================================
    // 日期範圍相關方法
    // ============================================

    /**
     * 取得最近 N 天的日期列表（包含今天）
     * @param days 天數
     * @return 日期列表，從最舊到最新排序
     *
     * 範例：days = 7, today = 2026-01-24
     * → [2026-01-18, 2026-01-19, ..., 2026-01-23, 2026-01-24]
     */
    @JvmStatic
    fun getRecentDates(days: Int): List<LocalDate> {
        require(days > 0) { "天數必須大於 0" }

        val today = LocalDate.now()
        return (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
    }

    /**
     * 取得過去 N 天的日期列表（不包含今天）
     * @param days 天數
     * @return 日期列表，從最舊到最新排序
     *
     * 範例：days = 7, today = 2026-01-24
     * → [2026-01-17, 2026-01-18, ..., 2026-01-22, 2026-01-23]
     */
    @JvmStatic
    fun getPastDates(days: Int): List<LocalDate> {
        require(days > 0) { "天數必須大於 0" }

        val today = LocalDate.now()
        return (days downTo 1).map { today.minusDays(it.toLong()) }
    }

    /**
     * 取得未來 N 天的日期列表（不包含今天）
     * @param days 天數
     * @return 日期列表，從最近到最遠排序
     *
     * 範例：days = 7, today = 2026-01-24
     * → [2026-01-25, 2026-01-26, ..., 2026-01-30, 2026-01-31]
     */
    @JvmStatic
    fun getFutureDates(days: Int): List<LocalDate> {
        require(days > 0) { "天數必須大於 0" }

        val today = LocalDate.now()
        return (1..days).map { today.plusDays(it.toLong()) }
    }

    /**
     * 取得兩個日期之間的所有日期（包含起始和結束日）
     * @param startDate 起始日期
     * @param endDate 結束日期
     * @return 日期列表，從起始到結束排序
     */
    @JvmStatic
    fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        require(!startDate.isAfter(endDate)) { "起始日期不能晚於結束日期" }

        val dates = mutableListOf<LocalDate>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            dates.add(current)
            current = current.plusDays(1)
        }
        return dates
    }

    // ============================================
    // 年份相關方法
    // ============================================

    /**
     * 取得本年的起始時間
     * @return 本年1月1日 00:00:00
     */
    @JvmStatic
    fun getStartOfCurrentYear(): LocalDateTime {
        return LocalDate.now()
            .withDayOfYear(1)  // 年初第1天
            .atStartOfDay()
    }

    /**
     * 取得本年的結束時間
     * @return 本年12月31日 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfCurrentYear(): LocalDateTime {
        return LocalDate.now()
            .withMonth(12)
            .withDayOfMonth(31)
            .atTime(23, 59, 59, 999_999_999)
    }

    // ============================================
    // 季度相關方法
    // ============================================

    /**
     * 取得當前季度的起始時間
     * @return 本季度第一天 00:00:00
     *
     * Q1: 01/01, Q2: 04/01, Q3: 07/01, Q4: 10/01
     */
    @JvmStatic
    fun getStartOfCurrentQuarter(): LocalDateTime {
        val today = LocalDate.now()
        val quarterStartMonth = ((today.monthValue - 1) / 3) * 3 + 1
        return today
            .withMonth(quarterStartMonth)
            .withDayOfMonth(1)
            .atStartOfDay()
    }

    /**
     * 取得當前季度的結束時間
     * @return 本季度最後一天 23:59:59.999999999
     */
    @JvmStatic
    fun getEndOfCurrentQuarter(): LocalDateTime {
        val today = LocalDate.now()
        val quarterEndMonth = ((today.monthValue - 1) / 3) * 3 + 3
        return today
            .withMonth(quarterEndMonth)
            .with(TemporalAdjusters.lastDayOfMonth())
            .atTime(23, 59, 59, 999_999_999)
    }

    // ============================================
    // 判斷方法
    // ============================================

    /**
     * 判斷日期是否在今天
     */
    @JvmStatic
    fun isToday(date: LocalDate): Boolean {
        return date == LocalDate.now()
    }

    /**
     * 判斷日期時間是否在今天
     */
    @JvmStatic
    fun isToday(dateTime: LocalDateTime): Boolean {
        return dateTime.toLocalDate() == LocalDate.now()
    }

    /**
     * 判斷日期是否在本週
     */
    @JvmStatic
    fun isThisWeek(date: LocalDate): Boolean {
        val weekStart = getStartOfCurrentWeek().toLocalDate()
        val weekEnd = getEndOfCurrentWeek().toLocalDate()
        return !date.isBefore(weekStart) && !date.isAfter(weekEnd)
    }

    /**
     * 判斷日期是否在本月
     */
    @JvmStatic
    fun isThisMonth(date: LocalDate): Boolean {
        val now = LocalDate.now()
        return date.year == now.year && date.month == now.month
    }

    /**
     * 判斷日期是否在本年
     */
    @JvmStatic
    fun isThisYear(date: LocalDate): Boolean {
        return date.year == LocalDate.now().year
    }
}
