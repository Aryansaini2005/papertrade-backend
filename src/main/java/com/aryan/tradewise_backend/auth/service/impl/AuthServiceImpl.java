package com.aryan.tradewise_backend.auth.service.impl;

import com.aryan.tradewise_backend.auth.dto.RegisterRequest;
import com.aryan.tradewise_backend.auth.dto.RegisterResponse;
import com.aryan.tradewise_backend.auth.service.AuthService;
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
        return null;
    }


}
