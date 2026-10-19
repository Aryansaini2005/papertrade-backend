
package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AICacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

@Service
public class AICacheServiceImpl implements AICacheService {

    private static final String CACHE_PREFIX = "ai-analysis:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private static final int MAX_REASONING_LENGTH = 2000;
    private static final int MAX_ANALYSIS_LENGTH = 5000;
    private static final int MAX_INVALIDATION_LENGTH = 1000;

    private static final Set<String> VALID_RECOMMENDATIONS =
            Set.of("BUY", "HOLD", "SELL");

    private static final Set<String> VALID_RISK_LEVELS =
            Set.of("LOW", "MEDIUM", "HIGH");

    private static final Set<String> VALID_HOLDING_PERIODS =
            Set.of("SHORT_TERM", "MEDIUM_TERM", "LONG_TERM");

    private static final Logger log =
            LoggerFactory.getLogger(AICacheServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public AICacheServiceImpl(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AIAnalysisResponse getAnalysis(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);

        if (normalizedSymbol == null) {
            return null;
        }

        String key = CACHE_PREFIX + normalizedSymbol;

        try {
            Object cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                return null;
            }

            AIAnalysisResponse response;

            if (cachedValue instanceof AIAnalysisResponse cachedResponse) {
                response = cachedResponse;
            } else {
                response = objectMapper.convertValue(
                        cachedValue,
                        AIAnalysisResponse.class
                );
            }

            if (!isValidCachedAnalysis(response, normalizedSymbol)) {
                evictAnalysis(normalizedSymbol);
                return null;
            }

            return response;

        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to read AI analysis cache for symbol {}. "
                            + "Continuing without cache.",
                    normalizedSymbol,
                    exception
            );
            return null;
        }
    }

    @Override
    public void saveAnalysis(
            String symbol,
            AIAnalysisResponse analysis) {

        String normalizedSymbol = normalizeSymbol(symbol);

        if (normalizedSymbol == null) {
            log.warn("Skipping AI cache write because symbol is invalid");
            return;
        }

        if (!isValidCachedAnalysis(analysis, normalizedSymbol)) {
            log.warn(
                    "Skipping invalid AI analysis cache write for symbol {}",
                    normalizedSymbol
            );
            return;
        }

        try {
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + normalizedSymbol,
                    analysis,
                    CACHE_TTL
            );

        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to cache AI analysis for symbol {}. "
                            + "Returning fresh analysis without caching.",
                    normalizedSymbol,
                    exception
            );
        }
    }

    @Override
    public void evictAnalysis(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);

        if (normalizedSymbol == null) {
            return;
        }

        try {
            redisTemplate.delete(CACHE_PREFIX + normalizedSymbol);

        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to evict AI analysis cache for symbol {}",
                    normalizedSymbol,
                    exception
            );
        }
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }

        return symbol.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isValidCachedAnalysis(
            AIAnalysisResponse response,
            String expectedSymbol) {

        if (response == null) {
            return false;
        }

        return response.getSymbol() != null
                && response.getSymbol().equalsIgnoreCase(expectedSymbol)

                && response.getCurrentPrice() != null
                && response.getCurrentPrice().signum() > 0

                && response.getRecommendation() != null
                && VALID_RECOMMENDATIONS.contains(
                response.getRecommendation().trim().toUpperCase(Locale.ROOT))

                && response.getReasoning() != null
                && !response.getReasoning().isBlank()
                && response.getReasoning().length() <= MAX_REASONING_LENGTH

                && response.getRiskLevel() != null
                && VALID_RISK_LEVELS.contains(
                response.getRiskLevel().trim().toUpperCase(Locale.ROOT))

                && response.getAnalysis() != null
                && !response.getAnalysis().isBlank()
                && response.getAnalysis().length() <= MAX_ANALYSIS_LENGTH

                && response.getHoldingPeriod() != null
                && VALID_HOLDING_PERIODS.contains(
                response.getHoldingPeriod().trim().toUpperCase(Locale.ROOT))

                && response.getInvalidationCondition() != null
                && !response.getInvalidationCondition().isBlank()
                && response.getInvalidationCondition().length()
                <= MAX_INVALIDATION_LENGTH

                && response.getDisclaimer() != null
                && !response.getDisclaimer().isBlank()

                && response.getGeneratedAt() != null

                && Double.isFinite(response.getProbability())
                && response.getProbability() >= 0
                && response.getProbability() <= 100;
    }
}
