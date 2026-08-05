package com.aryan.tradewise_backend.auth.service.impl;

import com.aryan.tradewise_backend.auth.dto.RegisterRequest;
import com.aryan.tradewise_backend.auth.dto.RegisterResponse;
import com.aryan.tradewise_backend.auth.service.AuthService;
import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.enums.Provider;
import com.aryan.tradewise_backend.user.enums.Role;
import com.aryan.tradewise_backend.user.enums.Status;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;




@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public RegisterResponse register(RegisterRequest request){

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword()) // Temporary, we'll encrypt later
                .role(Role.USER)
                .status(Status.ACTIVE)
                .provider(Provider.LOCAL)
                .build();
        // save to database
        User savedUser = userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setFirstName(savedUser.getFirstName());
        response.setEmail(savedUser.getEmail());
        response.setMessage("Registration successful.");

        return response;
    }


}
