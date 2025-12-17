package com.foodweb.foodapi.io;

import com.foodweb.foodapi.entity.FoodEntity;
import com.foodweb.foodapi.entity.OrderEntity;
import com.foodweb.foodapi.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name="order_item")
public class OrderItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "food_id", nullable = false)
        private FoodEntity food;

        private int quantity;

        // Snapshot fields
        private double price;
        private String name;
        private String category;
        private String imageUrl;
        private String description;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id")
        private OrderEntity order;

}


