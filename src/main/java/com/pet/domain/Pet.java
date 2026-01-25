package com.pet.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
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

    @Column(length = 500, name = "special_needs")
    private String specialNeeds;

    @Column(name = "is_neutered")
    private Boolean isNeutered;

    @Column(name = "vaccine_status")
    private String vaccineStatus;

    /**
     * 取得寵物類型名稱
     */
    public abstract String getPetTypeName();

    public enum Gender {
        MALE, FEMALE
    }
}
