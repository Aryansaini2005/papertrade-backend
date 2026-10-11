
        package com.aryan.tradewise_backend.ai.service;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;

public interface AIAnalysisService {

    AIAnalysisResponse analyzeStock(
            TechnicalAnalysisResponse technicalAnalysis
    );
}