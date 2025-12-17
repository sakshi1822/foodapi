package com.foodweb.foodapi.request;

import com.foodweb.foodapi.dto.OrderItemRequest;
import com.foodweb.foodapi.io.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderRequest {

    private List<OrderItemRequest> items;
    private String address;
    private String email;
    private String phone;
    private double amount;
    private String orderStatus;
}