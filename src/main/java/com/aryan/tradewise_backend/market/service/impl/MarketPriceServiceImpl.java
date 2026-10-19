
package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.service.MarketPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class MarketPriceServiceImpl implements MarketPriceService {

    private static final String PRICE_PREFIX = "market-price:";

    private static final Logger log =
            LoggerFactory.getLogger(MarketPriceServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public MarketPriceServiceImpl(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void updatePrice(String symbol, BigDecimal price) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol must not be blank"
            );
        }

        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Market price must be positive"
            );
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase(Locale.ROOT);

        try {
            redisTemplate.opsForValue().set(
                    PRICE_PREFIX + normalizedSymbol,
                    price
            );
        } catch (DataAccessException exception) {
            log.error(
                    "Failed to update market price in Redis for symbol {}",
                    normalizedSymbol,
                    exception
            );
            throw exception;
        }
    }

    @Override
    public BigDecimal getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase(Locale.ROOT);

        final Object value;

        try {
            value = redisTemplate.opsForValue().get(
                    PRICE_PREFIX + normalizedSymbol
            );
        } catch (DataAccessException exception) {
            log.error(
                    "Failed to retrieve market price from Redis for symbol {}",
                    normalizedSymbol,
                    exception
            );
            throw exception;
        }

        // No cached price exists for this symbol.
        if (value == null) {
            return null;
        }

        try {
            BigDecimal price = new BigDecimal(value.toString());

            if (price.signum() <= 0) {
                log.warn(
                        "Invalid non-positive market price found in Redis "
                                + "for symbol {}",
                        normalizedSymbol
                );
                return null;
            }

            return price;

        } catch (NumberFormatException exception) {
            log.warn(
                    "Malformed market price found in Redis for symbol {}",
                    normalizedSymbol,
                    exception
            );
            return null;
        }
    }
}
