package com.aryan.tradewise_backend.market.service;

import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;

public interface HistoricalPriceService {

    HistoricalPriceResponse getHistoricalPrices(
            String symbol,
            String interval
    );
}