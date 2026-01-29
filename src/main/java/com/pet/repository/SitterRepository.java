package com.pet.repository;

import com.pet.domain.Sitter;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SitterRepository extends JpaRepository<Sitter, UUID> {

    /**
     * 取得所有保母（使用 FETCH JOIN 預加載 user，避免 N+1 問題）
     */
    @Query("SELECT s FROM Sitter s JOIN FETCH s.user ORDER BY s.name ASC")
    List<Sitter> findAllWithUser();

    /**
     * 使用悲觀鎖查詢保母（MSSQL 相容版本）
     * 用於建立預約時防止時段衝突的 race condition
     * 鎖定保母資料列，確保同一時間只有一個 transaction 可以為該保母建立預約
     * 注意：MSSQL 使用 WITH (UPDLOCK, ROWLOCK) 而非 FOR UPDATE
     * 透過 QueryHint 設定 UPGRADE_SKIPLOCKED 讓 Hibernate 使用正確的 MSSQL 語法
     */
    Optional<Sitter> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT s FROM Sitter s WHERE s.id = :id")
    Optional<Sitter> findByIdWithLock(@Param("id") UUID id);
}
