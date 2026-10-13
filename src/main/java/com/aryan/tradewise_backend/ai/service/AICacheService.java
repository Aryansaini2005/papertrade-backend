package com.aryan.tradewise_backend.ai.service;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;

public interface AICacheService {

    AIAnalysisResponse getAnalysis(String symbol);

    void saveAnalysis(String symbol, AIAnalysisResponse analysis);
    void evictAnalysis(String symbol);
}
