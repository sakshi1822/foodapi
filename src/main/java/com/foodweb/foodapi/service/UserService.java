package com.foodweb.foodapi.service;

import com.foodweb.foodapi.io.UserRequest;
import com.foodweb.foodapi.io.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRequest request);
    Long findByUserId();
}
