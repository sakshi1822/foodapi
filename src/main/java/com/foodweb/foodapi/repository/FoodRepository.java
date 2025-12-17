package com.foodweb.foodapi.repository;

import com.foodweb.foodapi.entity.FoodEntity;
import jdk.jfr.Registered;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


@Repository
public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    List<FoodEntity> findByActiveTrue();

}
