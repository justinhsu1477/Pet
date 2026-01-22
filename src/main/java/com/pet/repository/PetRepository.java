package com.pet.repository;

import com.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {

    List<Pet> findByOwnerId(UUID userId);

    List<Pet> findByOwnerIdOrderByNameAsc(UUID userId);
}
