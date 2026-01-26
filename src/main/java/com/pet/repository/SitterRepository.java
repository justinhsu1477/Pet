package com.pet.repository;

import com.pet.domain.Sitter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SitterRepository extends JpaRepository<Sitter, UUID> {

    /**
     * 取得所有保母（使用 FETCH JOIN 預加載 user，避免 N+1 問題）
     */
    @Query("SELECT s FROM Sitter s JOIN FETCH s.user ORDER BY s.name ASC")
    List<Sitter> findAllWithUser();
}
