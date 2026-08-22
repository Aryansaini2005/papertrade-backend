package com.aryan.tradewise_backend.user.service;

import com.aryan.tradewise_backend.user.dto.WalletResponse;
import com.aryan.tradewise_backend.user.entity.Wallet;

public interface WalletService {

    WalletResponse getMyWallet();
    void resetIfNeeded(Wallet wallet);
}