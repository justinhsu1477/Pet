package com.pet.pricing

import com.pet.domain.ExperienceLevel
import kotlin.math.ceil

/**
 * 標準保母計費策略
 * - 基礎時薪: 150-250元
 * - 套餐折扣: 半天無折扣，全天88折，過夜85折
 */
class StandardPricingStrategy : PricingStrategy {
    override fun calculatePrice(baseHourlyRate: Double, durationHours: Int): Double {
        val billingHours = when (durationHours) {
            4 -> 4.0      // 半天：按實際計費
            8 -> 7.0      // 全天：7小時費用 (折扣 12.5%)
            24 -> 20.0    // 過夜：20小時費用 (折扣 16.7%)
            else -> durationHours.toDouble()
        }
        return ceil(baseHourlyRate * billingHours)
    }

    override fun getDescription(): String = "標準保母 - 適合新手保母"

    override fun getSuggestedHourlyRate(): Pair<Double, Double> = Pair(150.0, 250.0)
}

/**
 * 資深保母計費策略
 * - 基礎時薪: 200-350元
 * - 套餐折扣: 半天無折扣，全天90折，過夜88折
 */
class SeniorPricingStrategy : PricingStrategy {
    override fun calculatePrice(baseHourlyRate: Double, durationHours: Int): Double {
        val billingHours = when (durationHours) {
            4 -> 4.0      // 半天：按實際計費
            8 -> 7.2      // 全天：7.2小時費用 (折扣 10%)
            24 -> 21.0    // 過夜：21小時費用 (折扣 12.5%)
            else -> durationHours.toDouble()
        }
        return ceil(baseHourlyRate * billingHours)
    }

    override fun getDescription(): String = "資深保母 - 2年以上經驗"

    override fun getSuggestedHourlyRate(): Pair<Double, Double> = Pair(200.0, 350.0)
}

/**
 * 專家保母計費策略
 * - 基礎時薪: 300-500元
 * - 套餐折扣: 半天無折扣，全天92折，過夜90折
 */
class ExpertPricingStrategy : PricingStrategy {
    override fun calculatePrice(baseHourlyRate: Double, durationHours: Int): Double {
        val billingHours = when (durationHours) {
            4 -> 4.0      // 半天：按實際計費
            8 -> 7.4      // 全天：7.4小時費用 (折扣 7.5%)
            24 -> 21.6    // 過夜：21.6小時費用 (折扣 10%)
            else -> durationHours.toDouble()
        }
        return ceil(baseHourlyRate * billingHours)
    }

    override fun getDescription(): String = "專家保母 - 5年以上經驗或專業認證"

    override fun getSuggestedHourlyRate(): Pair<Double, Double> = Pair(300.0, 500.0)
}

/**
 * 計費策略工廠
 */
object PricingStrategyFactory {
    fun getStrategy(experienceLevel: ExperienceLevel): PricingStrategy {
        return when (experienceLevel) {
            ExperienceLevel.STANDARD -> StandardPricingStrategy()
            ExperienceLevel.SENIOR -> SeniorPricingStrategy()
            ExperienceLevel.EXPERT -> ExpertPricingStrategy()
        }
    }

    /**
     * 計算預約費用
     */
    @JvmStatic
    fun calculateBookingPrice(
        experienceLevel: ExperienceLevel,
        hourlyRate: Double,
        durationHours: Int
    ): Double {
        val strategy = getStrategy(experienceLevel)
        return strategy.calculatePrice(hourlyRate, durationHours)
    }
}
