package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AICacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AICacheServiceImpl implements AICacheService {

    private static final String CACHE_PREFIX = "ai-analysis:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

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
        String key = CACHE_PREFIX + symbol;

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

            // Treat malformed or incomplete cache entries as cache misses.
            if (!isValidCachedAnalysis(response)) {
                evictAnalysis(symbol);
                return null;
            }

            return response;

        } catch (RuntimeException exception) {
            // Redis or cache deserialization failed.
            // Continue without a cached result.
            return null;
        }
    }

    @Override
    public void saveAnalysis(
            String symbol,
            AIAnalysisResponse analysis) {

        try {
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + symbol,
                    analysis,
                    CACHE_TTL
            );
        } catch (RuntimeException exception) {
            // A cache write failure must not discard fresh AI analysis.
        }
    }

    @Override
    public void evictAnalysis(String symbol) {
        try {
            redisTemplate.delete(CACHE_PREFIX + symbol);
        } catch (RuntimeException exception) {
            // Cache invalidation will also be bounded by the TTL.
        }
    }

    private boolean isValidCachedAnalysis(AIAnalysisResponse response) {
        return response != null
                && response.getSymbol() != null
                && response.getCurrentPrice() != null
                && response.getRecommendation() != null
                && response.getReasoning() != null
                && !response.getReasoning().isBlank()
                && response.getRiskLevel() != null
                && response.getAnalysis() != null
                && !response.getAnalysis().isBlank()
                && response.getHoldingPeriod() != null
                && response.getInvalidationCondition() != null
                && !response.getInvalidationCondition().isBlank()
                && Double.isFinite(response.getProbability())
                && response.getProbability() >= 0
                && response.getProbability() <= 100;
    }
}
