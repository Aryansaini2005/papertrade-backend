package com.aryan.tradewise_backend.trade.service.impl;

import com.aryan.tradewise_backend.security.CurrentUserService;
import com.aryan.tradewise_backend.trade.dto.TradeResponse;
import com.aryan.tradewise_backend.trade.entity.Trade;
import com.aryan.tradewise_backend.trade.repository.TradeRepository;
import com.aryan.tradewise_backend.trade.service.TradeService;
import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public TradeServiceImpl(
            TradeRepository tradeRepository,
            CurrentUserService currentUserService,
            UserRepository userRepository) {

        this.tradeRepository = tradeRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Override
    public List<TradeResponse> getMyTrades() {

        String email = currentUserService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Trade> trades =
                tradeRepository.findByUserOrderByExecutedAtDesc(user);

        return trades.stream()
                .map(trade -> {

                    BigDecimal totalAmount =
                            trade.getQuantity()
                                    .multiply(trade.getExecutionPrice());

                    return TradeResponse.builder()
                            .id(trade.getId())
                            .assetSymbol(trade.getAsset().getSymbol())
                            .assetName(trade.getAsset().getName())
                            .orderType(trade.getOrderType())
                            .quantity(trade.getQuantity())
                            .executionPrice(trade.getExecutionPrice())
                            .totalAmount(totalAmount)
                            .executedAt(trade.getExecutedAt())
                            .build();
                })
                .toList();
    }
}