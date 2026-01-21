package com.pet.repository;

import com.pet.domain.SitterRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SitterRatingRepository extends JpaRepository<SitterRating, UUID> {

    /**
     * 檢查預約是否已經評價過（防止重複評價）
     */
    boolean existsByBookingId(UUID bookingId);

    /**
     * 根據預約ID查詢評價
     */
    Optional<SitterRating> findByBookingId(UUID bookingId);

    /**
     * 查詢保母的所有評價（分頁）
     */
    Page<SitterRating> findBySitterIdOrderByCreatedAtDesc(UUID sitterId, Pageable pageable);

    /**
     * 查詢保母的所有評價（不分頁）
     */
    List<SitterRating> findBySitterIdOrderByCreatedAtDesc(UUID sitterId);

    /**
     * 計算保母的平均評分
     */
    @Query("SELECT AVG(r.overallRating) FROM SitterRating r WHERE r.sitter.id = :sitterId")
    Double calculateAverageRating(@Param("sitterId") UUID sitterId);

    /**
     * 計算保母的加權平均評分
     */
    @Query("SELECT AVG(" +
           "  r.overallRating * 0.4 + " +
           "  COALESCE(r.professionalismRating, r.overallRating) * 0.25 + " +
           "  COALESCE(r.communicationRating, r.overallRating) * 0.20 + " +
           "  COALESCE(r.punctualityRating, r.overallRating) * 0.15" +
           ") FROM SitterRating r WHERE r.sitter.id = :sitterId")
    Double calculateWeightedAverageRating(@Param("sitterId") UUID sitterId);

    /**
     * 統計保母的評價數量
     */
    long countBySitterId(UUID sitterId);

    /**
     * 統計各星級的評價數量
     */
    @Query("SELECT r.overallRating, COUNT(r) FROM SitterRating r " +
           "WHERE r.sitter.id = :sitterId " +
           "GROUP BY r.overallRating")
    List<Object[]> countRatingsByStars(@Param("sitterId") UUID sitterId);

    /**
     * 計算各項細項的平均評分
     */
    @Query("SELECT " +
           "  AVG(r.overallRating), " +
           "  AVG(r.professionalismRating), " +
           "  AVG(r.communicationRating), " +
           "  AVG(r.punctualityRating) " +
           "FROM SitterRating r WHERE r.sitter.id = :sitterId")
    Object[] calculateDetailedAverages(@Param("sitterId") UUID sitterId);

    /**
     * 查詢使用者給出的所有評價
     */
    List<SitterRating> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
