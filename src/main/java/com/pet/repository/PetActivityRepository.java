package com.pet.repository;

import com.pet.domain.PetActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetActivityRepository extends JpaRepository<PetActivity, UUID> {

    /**
     * 根據寵物ID和日期查詢活動記錄
     */
    Optional<PetActivity> findByPetIdAndActivityDate(UUID petId, LocalDate activityDate);

    /**
     * 根據寵物ID查詢所有活動記錄（按日期降序排列）
     */
    List<PetActivity> findByPetIdOrderByActivityDateDesc(UUID petId);
}
