package com.pet.repository;

import com.pet.domain.Customer;
import com.pet.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
