package com.foodweb.foodapi.repository;

import com.foodweb.foodapi.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    List<OrderEntity> findByUserId(Long userId);
    Optional<OrderEntity> findByRazorpayOrderId(String razorpayOrderId);
    List<OrderEntity> findByOrderStatusNot(String orderStatus);

}
