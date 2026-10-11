package com.aryan.tradewise_backend.market.service;

import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

@Service
public class TechnicalIndicators {

    public BigDecimal calculateSMA(
            List<HistoricalPriceResponse.Candle> candles,
            int period) {

        if (candles == null || candles.size() < period) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;

        // Twelve Data usually returns newest candles first.
        // Use the most recent 'period' candles.
        for (int i = 0; i < period; i++) {
            sum = sum.add(
                    new BigDecimal(candles.get(i).getClose())
            );
        }

        return sum.divide(
                BigDecimal.valueOf(period),
                4,
                java.math.RoundingMode.HALF_UP
        );
    }

    public BigDecimal calculatePriceChangePercent(
            List<HistoricalPriceResponse.Candle> candles) {

        if (candles == null || candles.size() < 2) {
            return null;
        }

        BigDecimal newestClose =
                new BigDecimal(candles.get(0).getClose());

        BigDecimal oldestClose =
                new BigDecimal(
                        candles.get(candles.size() - 1).getClose()
                );

        if (oldestClose.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return newestClose.subtract(oldestClose)
                .multiply(BigDecimal.valueOf(100))
                .divide(oldestClose, MathContext.DECIMAL64);
    }
}
