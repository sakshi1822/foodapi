package com.foodweb.foodapi.controller;

import com.foodweb.foodapi.io.UserRequest;
import com.foodweb.foodapi.io.UserResponse;
import com.foodweb.foodapi.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody UserRequest request){
        UserResponse response = userService.registerUser(request);
        return response;

    }


}
