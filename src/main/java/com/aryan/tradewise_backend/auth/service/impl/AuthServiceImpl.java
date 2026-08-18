package com.aryan.tradewise_backend.auth.service.impl;

import com.aryan.tradewise_backend.auth.dto.LoginRequest;
import com.aryan.tradewise_backend.auth.dto.LoginResponse;
import com.aryan.tradewise_backend.auth.dto.RegisterRequest;
import com.aryan.tradewise_backend.auth.dto.RegisterResponse;
import com.aryan.tradewise_backend.auth.service.AuthService;
import com.aryan.tradewise_backend.common.exception.EmailAlreadyExistsException;
import com.aryan.tradewise_backend.common.exception.InvalidCredentialsException;
import com.aryan.tradewise_backend.security.JwtService;
import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.entity.Wallet;
import com.aryan.tradewise_backend.user.enums.Provider;
import com.aryan.tradewise_backend.user.enums.Role;
import com.aryan.tradewise_backend.user.enums.Status;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import com.aryan.tradewise_backend.user.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;


@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletRepository walletRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request){

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email already exists.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .status(Status.ACTIVE)
                .provider(Provider.LOCAL)
                .build();
        // save to database
        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .dailyLimit(new BigDecimal("100000.00"))
                .availableBalance(new BigDecimal("100000.00"))
                .lockedBalance(BigDecimal.ZERO)
                .currency("INR")
                .lastResetDate(LocalDate.now())
                .build();

        walletRepository.save(wallet);

        RegisterResponse response = new RegisterResponse();
        response.setFirstName(savedUser.getFirstName());
        response.setEmail(savedUser.getEmail());
        response.setMessage("Registration successful.");

        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {


        Optional<User> userOptional =
                userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);

        return response;
    }


}
