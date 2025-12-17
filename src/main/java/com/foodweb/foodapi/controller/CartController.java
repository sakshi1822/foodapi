package com.foodweb.foodapi.controller;

import com.foodweb.foodapi.io.AddToCartRequest;
import com.foodweb.foodapi.io.CartResponse;
import com.foodweb.foodapi.repository.UserRepository;
import com.foodweb.foodapi.service.CartService;
import com.foodweb.foodapi.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        if (request == null || request.getFoodId() == null) {
            return ResponseEntity.badRequest().body("foodId is required");
        }

        try {
            CartResponse response = cartService.addToCart(request);
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace(); // remove in production
            return ResponseEntity.internalServerError().body("Something went wrong while adding item to cart");
        }
    }
    @GetMapping
    public CartResponse getCart(){
        return cartService.getCart();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(){
        cartService.clearCart();
    }

    @PostMapping("/remove")
    public CartResponse removeFromCart(@RequestBody AddToCartRequest cartRequest){
        Long foodId = cartRequest.getFoodId();
        if (foodId == null || foodId < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "foodId not found");
        }
        return cartService.removeFromCart(cartRequest);
    }




}

