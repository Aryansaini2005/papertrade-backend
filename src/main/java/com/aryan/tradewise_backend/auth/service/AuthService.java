package com.aryan.tradewise_backend.auth.service;

import com.aryan.tradewise_backend.auth.dto.RegisterRequest;
import com.aryan.tradewise_backend.auth.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
}
