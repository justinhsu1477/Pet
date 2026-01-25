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

    @Column(name = "is_walk_required")
    private Boolean isWalkRequired;

    @Column(name = "walk_frequency_per_day")
    private Integer walkFrequencyPerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_level")
    private TrainingLevel trainingLevel;

    @Column(name = "is_friendly_with_dogs")
    private Boolean isFriendlyWithDogs;

    @Column(name = "is_friendly_with_people")
    private Boolean isFriendlyWithPeople;

    @Column(name = "is_friendly_with_children")
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
