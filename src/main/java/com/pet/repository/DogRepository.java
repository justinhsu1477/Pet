package com.pet.repository;

import com.pet.domain.Dog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DogRepository extends JpaRepository<Dog, UUID> {

    // 狗特有的查詢方法
    List<Dog> findBySize(Dog.Size size);

    List<Dog> findByIsWalkRequiredTrue();

    List<Dog> findByTrainingLevel(Dog.TrainingLevel trainingLevel);

    List<Dog> findByIsFriendlyWithDogsTrue();

    List<Dog> findByIsFriendlyWithChildrenTrue();
}
