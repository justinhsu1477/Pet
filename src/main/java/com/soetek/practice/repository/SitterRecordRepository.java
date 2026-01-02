package com.soetek.practice.repository;

import com.soetek.practice.domain.SitterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SitterRecordRepository extends JpaRepository<SitterRecord, Long> {
    List<SitterRecord> findByPetId(Long petId);
    List<SitterRecord> findBySitterId(Long sitterId);
}
