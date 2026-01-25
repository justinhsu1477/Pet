package com.pet.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("CAT")
public class Cat extends Pet {

    // 貓特有屬性
    @Column(name = "is_indoor")
    private Boolean isIndoor;

    @Enumerated(EnumType.STRING)
    @Column(name = "litter_box_type")
    private LitterBoxType litterBoxType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scratching_habit")
    private ScratchingHabit scratchingHabit;

    @Override
    public String getPetTypeName() {
        return "貓";
    }

    public enum LitterBoxType {
        OPEN,           // 開放式
        COVERED,        // 有蓋式
        AUTOMATIC,      // 自動清理
        TOP_ENTRY       // 上開式
    }

    public enum ScratchingHabit {
        LOW,            // 很少抓
        MODERATE,       // 中等
        HIGH            // 經常抓
    }
}
