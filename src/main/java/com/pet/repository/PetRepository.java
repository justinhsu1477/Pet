package com.pet.repository;

import com.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {

    @Query("SELECT p FROM Pet p JOIN FETCH p.owner WHERE p.owner.id = :userId")
    List<Pet> findByOwnerId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Pet p JOIN FETCH p.owner WHERE p.owner.id = :userId ORDER BY p.name ASC")
    List<Pet> findByOwnerIdOrderByNameAsc(@Param("userId") UUID userId);
}
