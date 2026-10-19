
package com.aryan.tradewise_backend.ai.controller;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.aryan.tradewise_backend.ai.service.AICacheService;
import com.aryan.tradewise_backend.ai.service.TechnicalAnalysisService;
import com.aryan.tradewise_backend.market.entity.Asset;
import com.aryan.tradewise_backend.market.repository.AssetRepository;
import com.aryan.tradewise_backend.market.service.MarketPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {

    private static final Logger log =
            LoggerFactory.getLogger(AIAnalysisController.class);

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

        // 1. Normalize and validate the symbol.
        if (symbol == null || symbol.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase(Locale.ROOT);

        if (!normalizedSymbol.matches("[A-Z0-9]+(/[A-Z0-9]+)?")) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Verify that the asset exists and is active.
        Asset asset = assetRepository.findBySymbol(normalizedSymbol)
                .filter(Asset::isActive)
                .orElse(null);

        if (asset == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Read the latest market price.
        BigDecimal currentPrice =
                marketPriceService.getPrice(normalizedSymbol);

        if (currentPrice == null) {
            return ResponseEntity.notFound().build();
        }

        if (currentPrice.signum() <= 0) {
            log.error(
                    "Invalid market price returned for symbol {}",
                    normalizedSymbol
            );

            return ResponseEntity.internalServerError().build();
        }

        // 4. Reuse cached analysis only when the price matches
        // and the analysis is less than five minutes old.
        AIAnalysisResponse cachedAnalysis =
                aiCacheService.getAnalysis(normalizedSymbol);

        if (cachedAnalysis != null
                && cachedAnalysis.getCurrentPrice() != null
                && cachedAnalysis.getGeneratedAt() != null) {

            long ageInSeconds = Duration.between(
                    cachedAnalysis.getGeneratedAt(),
                    LocalDateTime.now()
            ).getSeconds();

            boolean priceMatches =
                    currentPrice.compareTo(
                            cachedAnalysis.getCurrentPrice()
                    ) == 0;

            boolean analysisIsFresh =
                    ageInSeconds >= 0 && ageInSeconds < 300;

            if (priceMatches && analysisIsFresh) {
                return ResponseEntity.ok(cachedAnalysis);
            }
        }

        // 5. Calculate technical indicators.
        TechnicalAnalysisResponse technicalAnalysis =
                technicalAnalysisService.analyzeTechnicalData(
                        normalizedSymbol,
                        currentPrice
                );

        // 6. Generate fresh AI analysis.
        AIAnalysisResponse analysis =
                aiAnalysisService.analyzeStock(technicalAnalysis);

        // 7. Verify the AI response matches the requested asset and price.
        if (analysis == null
                || !normalizedSymbol.equals(analysis.getSymbol())
                || analysis.getCurrentPrice() == null
                || analysis.getCurrentPrice().compareTo(currentPrice) != 0) {

            log.error(
                    "AI analysis response mismatch for symbol {}",
                    normalizedSymbol
            );

            throw new IllegalStateException(
                    "AI analysis response does not match the requested symbol or price"
            );
        }

        // 8. Cache the result for five minutes.
        aiCacheService.saveAnalysis(
                normalizedSymbol,
                analysis
        );

        return ResponseEntity.ok(analysis);
    }
}
