package com.aryan.tradewise_backend.ai.service;

import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;

import java.math.BigDecimal;

public interface TechnicalAnalysisService {

    TechnicalAnalysisResponse analyzeTechnicalData(
            String symbol,
            BigDecimal currentPrice
    );
}
