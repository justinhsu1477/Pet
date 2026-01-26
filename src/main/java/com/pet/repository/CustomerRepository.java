package com.pet.repository;

import com.pet.domain.Customer;
import com.pet.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * 根據 User 查找 Customer
     */
    Optional<Customer> findByUser(Users user);

    /**
     * 根據 User ID 查找 Customer
     */
    Optional<Customer> findByUserId(UUID userId);

    /**
     * 取得所有客戶（使用 FETCH JOIN 預加載 user，避免 N+1 問題）
     */
    @Query("SELECT c FROM Customer c JOIN FETCH c.user ORDER BY c.name ASC")
    List<Customer> findAllWithUser();
}
