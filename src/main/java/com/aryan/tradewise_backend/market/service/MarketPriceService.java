package com.aryan.tradewise_backend.market.service;

import java.math.BigDecimal;

public interface MarketPriceService {

    void updatePrice(String symbol, BigDecimal price);

    BigDecimal getPrice(String symbol);
}