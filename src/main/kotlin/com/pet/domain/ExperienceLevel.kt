package com.pet.domain

/**
 * 保母經驗等級
 */
enum class ExperienceLevel {
    /**
     * 標準保母 - 新手或經驗較少
     * 時薪範圍: 150-250元
     */
    STANDARD,

    /**
     * 資深保母 - 2年以上經驗
     * 時薪範圍: 200-350元
     */
    SENIOR,

    /**
     * 專家保母 - 5年以上經驗或專業認證
     * 時薪範圍: 300-500元
     */
    EXPERT
}
