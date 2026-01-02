package com.pet.repository;

import com.pet.domain.SitterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SitterRecordRepository extends JpaRepository<SitterRecord, Long> {
    List<SitterRecord> findByPetId(Long petId);
    List<SitterRecord> findBySitterId(Long sitterId);
}
