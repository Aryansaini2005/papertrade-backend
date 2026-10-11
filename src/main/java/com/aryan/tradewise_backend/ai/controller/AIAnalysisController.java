package com.aryan.tradewise_backend.ai.controller;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.aryan.tradewise_backend.ai.service.TechnicalAnalysisService;
import com.aryan.tradewise_backend.market.service.MarketPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;
    private final TechnicalAnalysisService technicalAnalysisService;
    private final MarketPriceService marketPriceService;

    public AIAnalysisController(
            AIAnalysisService aiAnalysisService,
            TechnicalAnalysisService technicalAnalysisService,
            MarketPriceService marketPriceService) {

        this.aiAnalysisService = aiAnalysisService;
        this.technicalAnalysisService = technicalAnalysisService;
        this.marketPriceService = marketPriceService;
    }

    @GetMapping("/analyze/{symbol}")
    public ResponseEntity<AIAnalysisResponse> analyzeStock(
            @PathVariable String symbol) {

        // Step 1: Normalize and validate the symbol
        String normalizedSymbol = symbol.trim().toUpperCase();

        if (!normalizedSymbol.matches("[A-Z0-9]+(/[A-Z0-9]+)?")) {
            return ResponseEntity.badRequest().build();
        }

        // Step 2: Get the current market price from Redis
        BigDecimal currentPrice =
                marketPriceService.getPrice(normalizedSymbol);

        if (currentPrice == null) {
            return ResponseEntity.notFound().build();
        }

        // Step 3: Calculate technical indicators
        TechnicalAnalysisResponse technicalAnalysis =
                technicalAnalysisService.analyzeTechnicalData(
                        normalizedSymbol,
                        currentPrice
                );

        // Step 4: Generate the AI recommendation
        AIAnalysisResponse analysis =
                aiAnalysisService.analyzeStock(technicalAnalysis);

        // Step 5: Return the structured response
        return ResponseEntity.ok(analysis);
    }
}
