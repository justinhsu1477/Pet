package com.pet.pricing

/**
 * 計費策略介面
 * 不同經驗等級的保母可以有不同的計費方式
 */
interface PricingStrategy {
    /**
     * 計算預約費用
     * @param baseHourlyRate 保母的基礎時薪
     * @param durationHours 預約時長（小時）
     * @return 總費用
     */
    fun calculatePrice(baseHourlyRate: Double, durationHours: Int): Double

    /**
     * 取得策略描述
     */
    fun getDescription(): String

    /**
     * 取得建議時薪範圍
     */
    fun getSuggestedHourlyRate(): Pair<Double, Double>
}
