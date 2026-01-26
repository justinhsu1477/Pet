package com.pet.android.util

import kotlin.math.ceil

/**
 * 計費工具類
 * 與後端 PricingStrategy 邏輯保持一致
 */
object PricingUtils {

    /**
     * 計算預約費用
     * @param hourlyRate 時薪
     * @param durationHours 時長（小時）
     * @param experienceLevel 經驗等級
     * @return 總費用
     */
    fun calculatePrice(
        hourlyRate: Double,
        durationHours: Int,
        experienceLevel: String?
    ): Double {
        val billingHours = when (experienceLevel) {
            "EXPERT" -> {
                when (durationHours) {
                    4 -> 4.0      // 半天：按實際計費
                    8 -> 7.4      // 全天：7.4小時費用 (折扣 7.5%)
                    24 -> 21.6    // 過夜：21.6小時費用 (折扣 10%)
                    else -> durationHours.toDouble()
                }
            }
            "SENIOR" -> {
                when (durationHours) {
                    4 -> 4.0      // 半天：按實際計費
                    8 -> 7.2      // 全天：7.2小時費用 (折扣 10%)
                    24 -> 21.0    // 過夜：21小時費用 (折扣 12.5%)
                    else -> durationHours.toDouble()
                }
            }
            else -> { // STANDARD
                when (durationHours) {
                    4 -> 4.0      // 半天：按實際計費
                    8 -> 7.0      // 全天：7小時費用 (折扣 12.5%)
                    24 -> 20.0    // 過夜：20小時費用 (折扣 16.7%)
                    else -> durationHours.toDouble()
                }
            }
        }
        return ceil(hourlyRate * billingHours)
    }

    /**
     * 格式化價格顯示
     */
    fun formatPrice(price: Double): String {
        return "NT$ ${price.toInt()}"
    }

    /**
     * 取得經驗等級顯示名稱
     */
    fun getExperienceLevelName(level: String?): String {
        return when (level) {
            "EXPERT" -> "專家保母"
            "SENIOR" -> "資深保母"
            "STANDARD" -> "標準保母"
            else -> "標準保母"
        }
    }

    /**
     * 取得折扣說明
     */
    fun getDiscountDescription(durationHours: Int, experienceLevel: String?): String {
        return when (experienceLevel) {
            "EXPERT" -> {
                when (durationHours) {
                    8 -> "全天優惠92折"
                    24 -> "過夜優惠90折"
                    else -> ""
                }
            }
            "SENIOR" -> {
                when (durationHours) {
                    8 -> "全天優惠90折"
                    24 -> "過夜優惠88折"
                    else -> ""
                }
            }
            else -> { // STANDARD
                when (durationHours) {
                    8 -> "全天優惠88折"
                    24 -> "過夜優惠85折"
                    else -> ""
                }
            }
        }
    }
}
