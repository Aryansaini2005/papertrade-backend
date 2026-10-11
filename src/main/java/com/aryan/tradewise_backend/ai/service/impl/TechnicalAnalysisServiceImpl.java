package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.TechnicalAnalysisService;
import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;
import com.aryan.tradewise_backend.market.service.HistoricalPriceService;
import com.aryan.tradewise_backend.market.service.TechnicalIndicators;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TechnicalAnalysisServiceImpl
        implements TechnicalAnalysisService {

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

        HistoricalPriceResponse history =
                historicalPriceService.getHistoricalPrices(
                        symbol,
                        "1day"
                );

        List<HistoricalPriceResponse.Candle> candles =
                history.getValues();

        BigDecimal sma20 =
                technicalIndicators.calculateSMA(candles, 20);

        BigDecimal sma50 =
                technicalIndicators.calculateSMA(candles, 50);

        BigDecimal priceChangePercent =
                technicalIndicators.calculatePriceChangePercent(candles);

        String trend = "INSUFFICIENT_DATA";

        if (sma20 != null && currentPrice != null) {
            if (currentPrice.compareTo(sma20) > 0) {
                trend = "ABOVE_SMA20";
            } else if (currentPrice.compareTo(sma20) < 0) {
                trend = "BELOW_SMA20";
            } else {
                trend = "AT_SMA20";
            }
        }

        return new TechnicalAnalysisResponse(
                symbol,
                currentPrice,
                sma20,
                sma50,
                priceChangePercent,
                trend
        );
    }
}
