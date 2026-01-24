package com.pet.repository;

import com.pet.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM Users u LEFT JOIN FETCH u.customer WHERE u.role = :role")
    java.util.List<Users> findByRole(@org.springframework.data.repository.query.Param("role") com.pet.domain.UserRole role);
}