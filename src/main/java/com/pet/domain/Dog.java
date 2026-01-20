package com.pet.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("DOG")
public class Dog extends Pet {

    // 狗特有屬性
    @Enumerated(EnumType.STRING)
    private Size size;

    private Boolean isWalkRequired;
    private Integer walkFrequencyPerDay;

    @Enumerated(EnumType.STRING)
    private TrainingLevel trainingLevel;

    private Boolean isFriendlyWithDogs;
    private Boolean isFriendlyWithPeople;
    private Boolean isFriendlyWithChildren;

    @Override
    public String getPetTypeName() {
        return "狗";
    }

    public enum Size {
        SMALL,      // 小型犬 (< 10kg)
        MEDIUM,     // 中型犬 (10-25kg)
        LARGE,      // 大型犬 (25-45kg)
        GIANT       // 巨型犬 (> 45kg)
    }

    public enum TrainingLevel {
        NONE,       // 未訓練
        BASIC,      // 基礎訓練 (坐下、等待)
        INTERMEDIATE, // 中級訓練
        ADVANCED    // 進階訓練
    }
}
