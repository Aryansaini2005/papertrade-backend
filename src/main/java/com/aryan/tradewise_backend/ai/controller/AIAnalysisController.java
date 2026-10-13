package com.aryan.tradewise_backend.ai.controller;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.aryan.tradewise_backend.ai.service.AICacheService;
import com.aryan.tradewise_backend.ai.service.TechnicalAnalysisService;
import com.aryan.tradewise_backend.market.entity.Asset;
import com.aryan.tradewise_backend.market.repository.AssetRepository;
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
    private final AssetRepository assetRepository;
    private final AICacheService aiCacheService;

    public AIAnalysisController(
            AIAnalysisService aiAnalysisService,
            TechnicalAnalysisService technicalAnalysisService,
            MarketPriceService marketPriceService,
            AssetRepository assetRepository,
            AICacheService aiCacheService) {

        this.aiAnalysisService = aiAnalysisService;
        this.technicalAnalysisService = technicalAnalysisService;
        this.marketPriceService = marketPriceService;
        this.assetRepository = assetRepository;
        this.aiCacheService = aiCacheService;
    }

    @GetMapping("/analyze/{symbol}")
    public ResponseEntity<AIAnalysisResponse> analyzeStock(
            @PathVariable String symbol) {

        // 1. Normalize and validate the symbol
        String normalizedSymbol = symbol.trim().toUpperCase();

        if (!normalizedSymbol.matches("[A-Z0-9]+(/[A-Z0-9]+)?")) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Verify that the asset exists and is active
        Asset asset = assetRepository.findBySymbol(normalizedSymbol)
                .filter(Asset::isActive)
                .orElse(null);

        if (asset == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Read the latest market price once
        BigDecimal currentPrice =
                marketPriceService.getPrice(normalizedSymbol);

        if (currentPrice == null) {
            return ResponseEntity.notFound().build();
        }

        // 4. Reuse cached analysis only when its price matches
        AIAnalysisResponse cachedAnalysis =
                aiCacheService.getAnalysis(normalizedSymbol);

        if (cachedAnalysis != null
                && cachedAnalysis.getCurrentPrice() != null
                && currentPrice.compareTo(
                cachedAnalysis.getCurrentPrice()) == 0) {

            return ResponseEntity.ok(cachedAnalysis);
        }

        // 5. Calculate technical indicators
        TechnicalAnalysisResponse technicalAnalysis =
                technicalAnalysisService.analyzeTechnicalData(
                        normalizedSymbol,
                        currentPrice
                );

        // 6. Generate fresh AI analysis
        AIAnalysisResponse analysis =
                aiAnalysisService.analyzeStock(technicalAnalysis);

        // 7. Cache the result for five minutes
        aiCacheService.saveAnalysis(
                normalizedSymbol,
                analysis
        );

        return ResponseEntity.ok(analysis);
    }
}
