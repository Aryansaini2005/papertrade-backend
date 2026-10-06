package com.aryan.tradewise_backend.market.service;

import java.math.BigDecimal;

public interface MarketPriceWebSocketService {

    void publishPrice(String symbol, BigDecimal price);
}