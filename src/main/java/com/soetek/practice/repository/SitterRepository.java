package com.soetek.practice.repository;

import com.soetek.practice.domain.Sitter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SitterRepository extends JpaRepository<Sitter, Long> {
}
