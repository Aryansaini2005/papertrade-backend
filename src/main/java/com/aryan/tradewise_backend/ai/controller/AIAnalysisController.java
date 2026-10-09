package com.aryan.tradewise_backend.ai.controller;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.aryan.tradewise_backend.market.service.MarketPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;
    private final MarketPriceService marketPriceService;

    public AIAnalysisController(
            AIAnalysisService aiAnalysisService,
            MarketPriceService marketPriceService) {

        this.aiAnalysisService = aiAnalysisService;
        this.marketPriceService = marketPriceService;
    }

    @GetMapping("/analyze/{symbol}")
    public ResponseEntity<AIAnalysisResponse> analyzeStock(
            @PathVariable String symbol) {

        // Normalize and validate symbol
        String normalizedSymbol = symbol.trim().toUpperCase();

        if (!normalizedSymbol.matches("[A-Z0-9]+(/[A-Z0-9]+)?")) {
            return ResponseEntity.badRequest().build();
        }

        // Fetch current market price
        BigDecimal currentPrice =
                marketPriceService.getPrice(normalizedSymbol);

        if (currentPrice == null) {
            return ResponseEntity.notFound().build();
        }

        // Generate AI analysis
        AIAnalysisResponse analysis =
                aiAnalysisService.analyzeStock(
                        normalizedSymbol,
                        currentPrice
                );

        return ResponseEntity.ok(analysis);
    }
}