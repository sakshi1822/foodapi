package com.foodweb.foodapi.Implementation;

import com.foodweb.foodapi.entity.CartEntity;
import com.foodweb.foodapi.entity.UserEntity;
import com.foodweb.foodapi.io.AddToCartRequest;
import com.foodweb.foodapi.io.CartResponse;
import com.foodweb.foodapi.repository.CartRepository;
import com.foodweb.foodapi.repository.FoodRepository;
import com.foodweb.foodapi.repository.UserRepository;
import com.foodweb.foodapi.service.CartService;
import com.foodweb.foodapi.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserService userService;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

//    @Override
//    public void addToCart(Long foodId) {
//        Long loggedInUserId = userService.findByUserId();
//        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
//        CartEntity entity = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
//        Map<Long, Integer> cartItems = entity.getItems();
//        cartItems.put(foodId, cartItems.getOrDefault(foodId,0) + 1);
//        entity.setItems(cartItems);
//        cartRepository.save(entity);
//
//    }

    @Override
    public CartResponse addToCart(AddToCartRequest request) {
        Long loggedInUserId = userService.findByUserId();

        // ✅ Step 1: Validate user exists
        if (!userRepository.existsById(loggedInUserId)) {
            throw new IllegalArgumentException("User not found with ID " + loggedInUserId);
        }

        // ✅ Step 2: Validate food exists
        if (!foodRepository.existsById(request.getFoodId())) {
            throw new IllegalArgumentException("Food item not found with ID " + request.getFoodId());
        }

        // ✅ Step 3: Fetch user (for mapping in CartEntity)
        UserEntity user = userRepository.findById(loggedInUserId).get();

        // ✅ Step 4: Get or create user's cart
        CartEntity cart = cartRepository.findByUser(user)
                .orElse(new CartEntity(user, new HashMap<>()));

        // ✅ Step 5: Increase quantity by 1 each time API is called
        Map<Long, Integer> cartItems = cart.getItems();
        cartItems.merge(request.getFoodId(), 1, Integer::sum);
        cart.setItems(cartItems);

        // ✅ Step 6: Save and prepare response
        CartEntity saved = cartRepository.save(cart);

        CartResponse response = new CartResponse();
        response.setId(saved.getId());
        response.setUserId(user.getId());
        response.setItems(saved.getItems());

        return response;
    }

    @Override
    public CartResponse getCart() {
        Long loggedInUserId = userService.findByUserId();

        // ✅ Step 1: Validate user exists
        UserEntity user = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID " + loggedInUserId));

        // ✅ Step 2: Find cart for that user
        Optional<CartEntity> optionalCart = cartRepository.findByUser(user);

        // ✅ Step 3: Build response
        CartResponse response = new CartResponse();

        if (optionalCart.isPresent()) {
            CartEntity cart = optionalCart.get();
            response.setId(cart.getId());
            response.setUserId(user.getId());
            response.setItems(cart.getItems());
        } else {
            // If no cart exists, return empty map instead of list
            response.setId(null);
            response.setUserId(user.getId());
            response.setItems(Collections.emptyMap());
        }

        return response;
    }

    @Override
    @Transactional
    public void clearCart() {
        Long loggedInUserId = userService.findByUserId();
        cartRepository.deleteByUserId(loggedInUserId);
    }

//    @Override
//    @Transactional
//    public CartResponse removeFromCart(AddToCartRequest cartRequest) {
//        Long loggedInUserId = userService.findByUserId();
//        CartEntity entity = cartRepository.findByUserId(loggedInUserId)
//                .orElseThrow(() -> new RuntimeException("Cart is not found"));
//        Map<Long,Integer>cartItems = entity.getItems();
//        if (cartItems.containsKey(cartRequest.getFoodId())){
//            int currentQty = cartItems.get(cartRequest.getFoodId());
//            if (currentQty> 0){
//                cartItems.put(cartRequest.getFoodId(), currentQty -1 );
//            } else {
//                cartItems.remove(cartRequest.getFoodId());
//            }
//            entity = cartRepository.save(entity);
//
//        }
//        return convertToResponse(entity);
//    }
@Override
@Transactional
public CartResponse removeFromCart(AddToCartRequest cartRequest) {
    Long loggedInUserId = userService.findByUserId();

    CartEntity entity = cartRepository.findByUserId(loggedInUserId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));

    Map<Long, Integer> cartItems = entity.getItems();

    Long foodId = cartRequest.getFoodId();

    if (cartItems.containsKey(foodId)) {
        int currentQty = cartItems.get(foodId);

        if (currentQty <= 1) {
            // REMOVE the item completely
            cartItems.remove(foodId);
        } else {
            // Decrease quantity
            cartItems.put(foodId, currentQty - 1);
        }
    }

    // If cart becomes empty → delete cart
    if (cartItems.isEmpty()) {
        cartRepository.delete(entity);
        return new CartResponse(); // empty response
    }

    entity = cartRepository.save(entity);

    return convertToResponse(entity);
}


    private CartResponse convertToResponse(CartEntity cartEntity){
        return CartResponse.builder()
                .id(cartEntity.getId())
                .userId(cartEntity.getUser().getId())
                .items(cartEntity.getItems())
                .build();
    }


}
