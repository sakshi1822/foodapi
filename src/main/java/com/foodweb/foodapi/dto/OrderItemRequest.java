package com.foodweb.foodapi.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long foodId;
    private int quantity;
}
