package com.aryan.tradewise_backend.user.service.impl;

import com.aryan.tradewise_backend.security.CurrentUserService;
import com.aryan.tradewise_backend.user.dto.WalletResponse;
import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.entity.Wallet;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import com.aryan.tradewise_backend.user.repository.WalletRepository;
import com.aryan.tradewise_backend.user.service.WalletService;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService) {

        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public WalletResponse getMyWallet() {

        String email = currentUserService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found"));

        return WalletResponse.builder()
                .id(wallet.getId())
                .dailyLimit(wallet.getDailyLimit())
                .availableBalance(wallet.getAvailableBalance())
                .lockedBalance(wallet.getLockedBalance())
                .currency(wallet.getCurrency())
                .build();
    }
}