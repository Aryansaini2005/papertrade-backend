package com.aryan.tradewise_backend.trade.repository;

import com.aryan.tradewise_backend.trade.entity.Trade;
import com.aryan.tradewise_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserOrderByExecutedAtDesc(User user);
}