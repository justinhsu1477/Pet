package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "pet_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users owner;

    private String name;
    private Integer age;
    private String breed;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String ownerName;
    private String ownerPhone;

    @Column(length = 500)
    private String specialNeeds;

    private Boolean isNeutered;
    private String vaccineStatus;

    /**
     * 取得寵物類型名稱
     */
    public abstract String getPetTypeName();

    public enum Gender {
        MALE, FEMALE
    }
}
