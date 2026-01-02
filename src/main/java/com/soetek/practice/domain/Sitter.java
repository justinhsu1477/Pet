package com.soetek.practice.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Sitter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;

    @Column(length = 500)
    private String experience;
}
