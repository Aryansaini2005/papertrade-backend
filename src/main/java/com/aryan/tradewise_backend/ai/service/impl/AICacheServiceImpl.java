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

        Object cachedValue = redisTemplate.opsForValue().get(
                CACHE_PREFIX + symbol
        );

        if (cachedValue == null) {
            return null;
        }

        try {
            if (cachedValue instanceof AIAnalysisResponse response) {
                return response;
            }

            return objectMapper.convertValue(
                    cachedValue,
                    AIAnalysisResponse.class
            );

        } catch (IllegalArgumentException exception) {
            // Remove corrupted or incompatible cached data.
            redisTemplate.delete(CACHE_PREFIX + symbol);
            return null;
        }
    }

    @Override
    public void saveAnalysis(
            String symbol,
            AIAnalysisResponse analysis) {

        redisTemplate.opsForValue().set(
                CACHE_PREFIX + symbol,
                analysis,
                CACHE_TTL
        );
    }
}
