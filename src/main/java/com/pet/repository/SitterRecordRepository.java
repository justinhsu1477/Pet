package com.pet.repository;

import com.pet.domain.SitterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SitterRecordRepository extends JpaRepository<SitterRecord, UUID> {

    /**
     * 根據寵物 ID 查詢記錄（使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT r FROM SitterRecord r " +
           "JOIN FETCH r.pet " +
           "JOIN FETCH r.sitter " +
           "WHERE r.pet.id = :petId " +
           "ORDER BY r.recordTime DESC")
    List<SitterRecord> findByPetId(@Param("petId") UUID petId);

    /**
     * 根據保母 ID 查詢記錄（使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT r FROM SitterRecord r " +
           "JOIN FETCH r.pet " +
           "JOIN FETCH r.sitter " +
           "WHERE r.sitter.id = :sitterId " +
           "ORDER BY r.recordTime DESC")
    List<SitterRecord> findBySitterId(@Param("sitterId") UUID sitterId);
}
