
package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.TechnicalAnalysisService;
import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;
import com.aryan.tradewise_backend.market.service.HistoricalPriceService;
import com.aryan.tradewise_backend.market.service.TechnicalIndicators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class TechnicalAnalysisServiceImpl
        implements TechnicalAnalysisService {

    private static final Logger log =
            LoggerFactory.getLogger(TechnicalAnalysisServiceImpl.class);

    private final HistoricalPriceService historicalPriceService;
    private final TechnicalIndicators technicalIndicators;

    public TechnicalAnalysisServiceImpl(
            HistoricalPriceService historicalPriceService,
            TechnicalIndicators technicalIndicators) {

        this.historicalPriceService = historicalPriceService;
        this.technicalIndicators = technicalIndicators;
    }

    @Override
    public TechnicalAnalysisResponse analyzeTechnicalData(
            String symbol,
            BigDecimal currentPrice) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol must not be blank"
            );
        }

        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Current market price must be positive"
            );
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase(Locale.ROOT);

        List<HistoricalPriceResponse.Candle> candles =
                Collections.emptyList();

        try {
            HistoricalPriceResponse history =
                    historicalPriceService.getHistoricalPrices(
                            normalizedSymbol,
                            "1day"
                    );

            if (history != null && history.getValues() != null) {
                candles = history.getValues();
            }

        } catch (RuntimeException exception) {
            log.warn(
                    "Historical data unavailable for {}. "
                            + "Continuing with unavailable indicators.",
                    normalizedSymbol,
                    exception
            );
        }

        BigDecimal sma20 = null;
        BigDecimal sma50 = null;
        BigDecimal priceChangePercent = null;

        if (!candles.isEmpty()) {
            sma20 = technicalIndicators.calculateSMA(candles, 20);
            sma50 = technicalIndicators.calculateSMA(candles, 50);
            priceChangePercent =
                    technicalIndicators.calculatePriceChangePercent(candles);
        }

        // Log each missing indicator independently.
        if (sma20 == null) {
            log.warn(
                    "SMA20 unavailable for {}: at least 20 valid candles are required.",
                    normalizedSymbol
            );
        }

        if (sma50 == null) {
            log.warn(
                    "SMA50 unavailable for {}: at least 50 valid candles are required.",
                    normalizedSymbol
            );
        }

        if (priceChangePercent == null) {
            log.warn(
                    "Price change percentage unavailable for {}.",
                    normalizedSymbol
            );
        }

        String trend = "INSUFFICIENT_DATA";

        if (sma20 != null) {
            int comparison = currentPrice.compareTo(sma20);

            if (comparison > 0) {
                trend = "ABOVE_SMA20";
            } else if (comparison < 0) {
                trend = "BELOW_SMA20";
            } else {
                trend = "AT_SMA20";
            }
        }

        return new TechnicalAnalysisResponse(
                normalizedSymbol,
                currentPrice,
                sma20,
                sma50,
                priceChangePercent,
                trend
        );
    }
}
