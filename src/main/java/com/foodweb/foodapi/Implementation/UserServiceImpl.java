package com.foodweb.foodapi.Implementation;

import com.foodweb.foodapi.entity.UserEntity;
import com.foodweb.foodapi.io.UserRequest;
import com.foodweb.foodapi.io.UserResponse;
import com.foodweb.foodapi.repository.UserRepository;
import com.foodweb.foodapi.service.AuthenticationFacade;
import com.foodweb.foodapi.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public UserResponse registerUser(UserRequest request) {
        UserEntity newUser = convertToEntity(request);
        newUser = userRepository.save(newUser);
        return convertToResponse(newUser);
    }

    @Override
    public Long findByUserId() {
       String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
       UserEntity loogedInUser = userRepository.findByEmail(loggedInUserEmail).orElseThrow(()->new UsernameNotFoundException("user not found"));
       return loogedInUser.getId();
    }

    private UserEntity convertToEntity(UserRequest request){
        return UserEntity.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
    }

    private UserResponse convertToResponse(UserEntity registerUser){
        return UserResponse.builder()
                .id(registerUser.getId())
                .name(registerUser.getName())
                .email(registerUser.getEmail())
                .build();
    }
}
