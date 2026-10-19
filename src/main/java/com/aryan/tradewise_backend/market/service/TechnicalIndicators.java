
package com.aryan.tradewise_backend.market.service;

import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TechnicalIndicators {

    public BigDecimal calculateSMA(
            List<HistoricalPriceResponse.Candle> candles,
            int period) {

        if (candles == null
                || period <= 0
                || candles.size() < period) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;

        // Twelve Data usually returns newest candles first.
        // Use the most recent 'period' candles.
        for (int i = 0; i < period; i++) {
            BigDecimal close = parseClose(candles.get(i));

            // Reject the entire window if any close is invalid.
            if (close == null) {
                return null;
            }

            sum = sum.add(close);
        }

        return sum.divide(
                BigDecimal.valueOf(period),
                4,
                RoundingMode.HALF_UP
        );
    }

    public BigDecimal calculatePriceChangePercent(
            List<HistoricalPriceResponse.Candle> candles) {

        if (candles == null || candles.size() < 2) {
            return null;
        }

        BigDecimal newestClose = parseClose(candles.get(0));
        BigDecimal oldestClose =
                parseClose(candles.get(candles.size() - 1));

        if (newestClose == null || oldestClose == null) {
            return null;
        }

        return newestClose.subtract(oldestClose)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        oldestClose,
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal parseClose(
            HistoricalPriceResponse.Candle candle) {

        if (candle == null
                || candle.getClose() == null
                || candle.getClose().isBlank()) {
            return null;
        }

        try {
            BigDecimal close = new BigDecimal(candle.getClose());

            return close.signum() > 0 ? close : null;

        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
