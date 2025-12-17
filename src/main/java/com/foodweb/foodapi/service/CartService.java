package com.foodweb.foodapi.service;

import com.foodweb.foodapi.io.AddToCartRequest;
import com.foodweb.foodapi.io.CartResponse;

public interface CartService {

    CartResponse addToCart(AddToCartRequest request);
    CartResponse getCart();
    void clearCart();
    CartResponse removeFromCart(AddToCartRequest cartRequest);
}
