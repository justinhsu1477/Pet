package com.soetek.practice.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private Integer age;
    private String breed;
    private String ownerName;
    private String ownerPhone;

    @Column(length = 500)
    private String specialNeeds;
}
