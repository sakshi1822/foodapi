package com.foodweb.foodapi.Implementation;

import com.foodweb.foodapi.dto.OrderItemRequest;
import com.foodweb.foodapi.dto.OrderItemResponse;
import com.foodweb.foodapi.entity.FoodEntity;
import com.foodweb.foodapi.entity.OrderEntity;
import com.foodweb.foodapi.entity.UserEntity;
import com.foodweb.foodapi.io.OrderItem;
import com.foodweb.foodapi.repository.CartRepository;
import com.foodweb.foodapi.repository.FoodRepository;
import com.foodweb.foodapi.repository.OrderRepository;
import com.foodweb.foodapi.repository.UserRepository;
import com.foodweb.foodapi.request.OrderRequest;
import com.foodweb.foodapi.response.OrderResponse;
import com.foodweb.foodapi.service.OrderService;
import com.foodweb.foodapi.service.UserService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private CartRepository cartRepository;
    @Value("${razorpay_key}")
    private String RAZORPAY_KEY;
    @Value("${razorpay_secret}")
    private String RAZORPAY_SECRET;

    @Override
    public OrderResponse createOrderWithPayment(OrderRequest request) throws RazorpayException {

        Long userId = userService.findByUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setAddress(request.getAddress());
        order.setEmail(request.getEmail());
        order.setPhone(request.getPhone());
        order.setOrderStatus("PREPARING");

        List<OrderItem> items = new ArrayList<>();
        double totalAmount = 0;

        for (OrderItemRequest itemReq : request.getItems()) {

            FoodEntity food = foodRepository.findById(itemReq.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found"));

            OrderItem item = new OrderItem();
            item.setFood(food);
            item.setQuantity(itemReq.getQuantity());
            item.setOrder(order);

            // snapshot fields
            item.setPrice(food.getPrice());
            item.setName(food.getName());
            item.setCategory(food.getCategory());
            item.setImageUrl(food.getImageUrl());
            item.setDescription(food.getDescription());

            items.add(item);

            totalAmount += food.getPrice() * itemReq.getQuantity();
            double shipping = 10;
            double tax = totalAmount * 0.10;
            totalAmount = totalAmount + shipping + tax;

        }

        order.setOrderedItems(items);
        order.setAmount(totalAmount);

        // save order first
        order = orderRepository.save(order);

        // Razorpay create order
        RazorpayClient client = new RazorpayClient(RAZORPAY_KEY, RAZORPAY_SECRET);

        JSONObject razorReq = new JSONObject();
        razorReq.put("amount", (int)(totalAmount * 100));
        razorReq.put("currency", "INR");
        razorReq.put("payment_capture", 1);

        Order razorpayOrder = client.orders.create(razorReq);
        order.setRazorpayOrderId(razorpayOrder.get("id"));
        order.setPaymentStatus("CREATED");

        order = orderRepository.save(order);

        return convertToResponse(order);
    }

    @Override
    public void verifyPayment(Map<String, String> paymentData, String status) {
        String razorpayOrderId = paymentData.get("razorpay_order_id");
        OrderEntity existingOrder = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        existingOrder.setPaymentStatus(status);
        existingOrder.setRazorpaySignature(paymentData.get("razorpay_signature"));
        existingOrder.setRazorpayOrderId(paymentData.get("razorpay_payment_id"));
        orderRepository.save(existingOrder);
        if("paid".equalsIgnoreCase(status)){
            cartRepository.deleteByUserId(existingOrder.getUser().getId());
        }
    }

    @Override
    public List<OrderResponse> getUserOrders() {
        Long loggedInUserId = userService.findByUserId();
        List<OrderEntity> list = orderRepository.findByUserId(loggedInUserId);
        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
    }

    @Override
    public void removeOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

//    @Override
//    public List<OrderResponse> getOrdersOfAll() {
//        List<OrderEntity> list = orderRepository.findAll();
//        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
//    }

    @Override
    @Transactional
    public List<OrderResponse> getPendingOrders() {
        List<OrderEntity> orders = orderRepository.findByOrderStatusNot("Delivered");

        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }


    @Override
    public void updateOrderStatus(Long orderId, String status) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("order not found"));
        entity.setOrderStatus(status);
        orderRepository.save(entity);
    }


    private OrderResponse convertToResponse(OrderEntity order) {

        List<OrderItemResponse> items = order.getOrderedItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .foodId(i.getFood().getId())
                        .name(i.getName())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .build()
                ).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .address(order.getAddress())
                .phone(order.getPhone())
                .email(order.getEmail())
                .amount(order.getAmount())
                .paymentStatus(order.getPaymentStatus())
                .razorpayOrderId(order.getRazorpayOrderId())
                .orderStatus(order.getOrderStatus())
                .items(items)
                .build();
    }


}
