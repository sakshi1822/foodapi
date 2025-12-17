package com.foodweb.foodapi.repository;

import com.foodweb.foodapi.entity.CartEntity;
import com.foodweb.foodapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
//    Optional<CartEntity> findByUserId(Long userId);

    Optional<CartEntity> findByUser(UserEntity user);

    @Query("SELECT c FROM CartEntity c WHERE c.user.id = :userId")
    Optional<CartEntity> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartEntity c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
