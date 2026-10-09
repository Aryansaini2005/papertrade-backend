package com.aryan.tradewise_backend.ai.service;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;

import java.math.BigDecimal;

public interface AIAnalysisService {

    AIAnalysisResponse analyzeStock(
            String symbol,
            BigDecimal currentPrice
    );
}