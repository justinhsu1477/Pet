package com.pet.repository;

import com.pet.domain.RefreshToken;
import com.pet.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * 根據 Token Hash 查找
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * 根據用戶和設備類型查找有效的 Token
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.deviceType = :deviceType AND rt.revoked = :revoked " +
           "AND rt.expiryDate > :now")
    Optional<RefreshToken> findValidTokenByUserAndDevice(
        @Param("user") Users user,
        @Param("deviceType") String deviceType,
        @Param("revoked") boolean revoked,
        @Param("now") LocalDateTime now
    );

    /**
     * 查找用戶的所有有效 Token
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId " +
           "AND rt.revoked = :revoked AND rt.expiryDate > :now")
    List<RefreshToken> findValidTokensByUser(
        @Param("userId") UUID userId,
        @Param("revoked") boolean revoked,
        @Param("now") LocalDateTime now
    );

    /**
     * 撤銷用戶的所有 Token
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = :revoked WHERE rt.user.id = :userId")
    void revokeAllUserTokens(
        @Param("userId") UUID userId,
        @Param("revoked") boolean revoked
    );

    /**
     * 撤銷用戶特定設備的所有 Token
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = :revoked " +
           "WHERE rt.user.id = :userId AND rt.deviceType = :deviceType")
    void revokeUserDeviceTokens(
        @Param("userId") UUID userId,
        @Param("deviceType") String deviceType,
        @Param("revoked") boolean revoked
    );

    /**
     * 刪除過期的 Token (定期清理任務用)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 刪除已撤銷且過期的 Token (定期清理任務用)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = :revoked AND rt.expiryDate < :now")
    int deleteRevokedExpiredTokens(
        @Param("revoked") boolean revoked,
        @Param("now") LocalDateTime now
    );

    /**
     * 刪除用戶的所有 Token
     */
    void deleteByUser(Users user);

    /**
     * 統計用戶活躍設備數量
     */
    @Query("SELECT COUNT(DISTINCT rt.deviceType) FROM RefreshToken rt " +
           "WHERE rt.user.id = :userId AND rt.revoked = :revoked " +
           "AND rt.expiryDate > :now")
    long countActiveDevicesByUser(
        @Param("userId") UUID userId,
        @Param("revoked") boolean revoked,
        @Param("now") LocalDateTime now
    );
}
