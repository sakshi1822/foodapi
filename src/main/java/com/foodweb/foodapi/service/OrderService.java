package com.foodweb.foodapi.service;

import com.foodweb.foodapi.request.OrderRequest;
import com.foodweb.foodapi.response.OrderResponse;
import com.razorpay.RazorpayException;

import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse createOrderWithPayment(OrderRequest request) throws RazorpayException;
    void verifyPayment(Map<String, String> paymentData, String status);
    List<OrderResponse> getUserOrders();
    void removeOrder(Long orderId);
    public List<OrderResponse> getPendingOrders();
     void updateOrderStatus(Long orderId,String status);
}
