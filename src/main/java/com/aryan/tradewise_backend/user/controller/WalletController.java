package com.aryan.tradewise_backend.user.controller;

import com.aryan.tradewise_backend.user.dto.WalletResponse;
import com.aryan.tradewise_backend.user.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<WalletResponse> getMyWallet() {

        WalletResponse response = walletService.getMyWallet();

        return ResponseEntity.ok(response);
    }
}