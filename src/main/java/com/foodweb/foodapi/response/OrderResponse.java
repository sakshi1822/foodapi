package com.foodweb.foodapi.response;

import com.foodweb.foodapi.dto.OrderItemResponse;
import com.foodweb.foodapi.entity.UserEntity;
import com.foodweb.foodapi.io.OrderItem;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String address;
    private String phone;
    private String email;

    private double amount;
    private String paymentStatus;
    private String razorpayOrderId;

    private String orderStatus;
    private List<OrderItemResponse> items;;
}
