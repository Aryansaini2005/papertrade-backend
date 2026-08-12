package com.aryan.tradewise_backend.user.repository;

import com.aryan.tradewise_backend.user.entity.Wallet;
import com.aryan.tradewise_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserId(Long userId);
}