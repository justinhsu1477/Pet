package com.pet.repository;

import com.pet.domain.Cat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CatRepository extends JpaRepository<Cat, UUID> {
    List<Cat> findByIsIndoorTrue();

    List<Cat> findByIsIndoorFalse();

    List<Cat> findByLitterBoxType(Cat.LitterBoxType litterBoxType);

    List<Cat> findByScratchingHabit(Cat.ScratchingHabit scratchingHabit);
}
