package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.service.MarketPriceService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MarketPriceServiceImpl implements MarketPriceService {

    private final RedisTemplate<String, Object> redisTemplate;

    public MarketPriceServiceImpl(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void updatePrice(String symbol, BigDecimal price) {
        redisTemplate.opsForValue().set(
                "market-price:" + symbol,
                price
        );
    }

    @Override
    public BigDecimal getPrice(String symbol) {

        Object value = redisTemplate.opsForValue().get(
                "market-price:" + symbol
        );

        if (value == null) {
            return null;
        }

        return new BigDecimal(value.toString());
    }
}