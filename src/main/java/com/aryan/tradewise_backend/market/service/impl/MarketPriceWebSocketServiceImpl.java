package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.service.MarketPriceWebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MarketPriceWebSocketServiceImpl
        implements MarketPriceWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public MarketPriceWebSocketServiceImpl(
            SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishPrice(String symbol, BigDecimal price) {

        messagingTemplate.convertAndSend(
                "/topic/market-price/" + symbol,
                price
        );
    }
}