package com.pet.repository;

import com.pet.domain.SitterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SitterRecordRepository extends JpaRepository<SitterRecord, UUID> {
    List<SitterRecord> findByPetId(UUID petId);
    List<SitterRecord> findBySitterId(UUID sitterId);
}
