package com.pet.repository;

import com.pet.domain.SitterAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SitterAvailabilityRepository extends JpaRepository<SitterAvailability, UUID> {

    /**
     * 查詢保母的所有可用時段
     */
    List<SitterAvailability> findBySitterIdAndIsActiveTrue(UUID sitterId);

    /**
     * 查詢特定星期的可用保母
     */
    @Query("SELECT sa FROM SitterAvailability sa " +
           "WHERE sa.dayOfWeek = :dayOfWeek " +
           "AND sa.isActive = true " +
           "AND sa.startTime <= :time " +
           "AND sa.endTime >= :time")
    List<SitterAvailability> findAvailableSitters(
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("time") LocalTime time);

    /**
     * 查詢特定地區的可用保母
     */
    List<SitterAvailability> findByServiceAreaContainingAndIsActiveTrue(String area);

    /**
     * 檢查保母在特定時段是否有設定可用
     */
    @Query("SELECT COUNT(sa) > 0 FROM SitterAvailability sa " +
           "WHERE sa.sitter.id = :sitterId " +
           "AND sa.dayOfWeek = :dayOfWeek " +
           "AND sa.startTime <= :startTime " +
           "AND sa.endTime >= :endTime " +
           "AND sa.isActive = true")
    boolean isSitterAvailable(
            @Param("sitterId") UUID sitterId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
