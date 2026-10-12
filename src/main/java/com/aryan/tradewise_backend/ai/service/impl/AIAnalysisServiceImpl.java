package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    public AIAnalysisServiceImpl(
            OpenAIClient openAIClient,
            ObjectMapper objectMapper) {

        this.openAIClient = openAIClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public AIAnalysisResponse analyzeStock(
            TechnicalAnalysisResponse technicalAnalysis) {

        String prompt = """
                You are the AI analysis assistant for TradeWise,
                an educational paper-trading platform.

                Market data:
                Symbol: %s
                Current price: %s
                SMA 20: %s
                SMA 50: %s
                Price change percentage: %s
                Trend: %s

                Data interpretation rules:
                - A value of null means unavailable; it does not mean zero.
                - Do not calculate missing indicators from insufficient data.
                - Distinguish observed market data from interpretations.
                - If key data is missing, lower confidence and explain
                  the limitation.

                Return ONLY valid JSON with these fields:
                {
                  "recommendation": "BUY, HOLD, or SELL",
                  "reasoning": "Evidence supporting the recommendation",
                  "probability": 0,
                  "riskLevel": "LOW, MEDIUM, or HIGH",
                  "analysis": "Detailed educational analysis",
                  "holdingPeriod": "SHORT_TERM, MEDIUM_TERM, or LONG_TERM",
                  "invalidationCondition": "Condition that would weaken or invalidate the analysis"
                }

                Rules:
                - Probability must be between 0 and 100.
                - Probability represents subjective model confidence,
                  NOT the probability of profit.
                - Treat missing indicators as unavailable, not zero.
                - Do not invent prices, news, or market indicators.
                - Explain uncertainty and relevant risks.
                - Do not invent precise entry prices, targets, or stop-losses.
                - If the evidence is insufficient, prefer HOLD and explain why.
                - Choose a holding period only as an educational analysis
                  horizon; do not imply certainty about future price movement.
                - Make the invalidation condition evidence-based. If there
                  is insufficient evidence, say that more data is needed.
                - Never guarantee returns.
                - This is educational paper-trading analysis, not
                  financial advice.
                """.formatted(
                technicalAnalysis.getSymbol(),
                technicalAnalysis.getCurrentPrice(),
                technicalAnalysis.getSma20(),
                technicalAnalysis.getSma50(),
                technicalAnalysis.getPriceChangePercent(),
                technicalAnalysis.getTrend()
        );

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model("gpt-5-mini")
                .input(prompt)
                .build();

        Response response = openAIClient.responses().create(params);

        String json = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst()
                .map(outputText -> outputText.text())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "OpenAI returned no analysis"
                        )
                );

        // Remove optional Markdown code fences.
        json = json.trim();

        if (json.startsWith("```")) {
            json = json.replaceFirst("^```(?:json)?\\s*", "");
            json = json.replaceFirst("\\s*```$", "");
        }

        try {
            AIAnalysisResponse aiResponse =
                    objectMapper.readValue(
                            json,
                            AIAnalysisResponse.class
                    );

            validateAIResponse(aiResponse);

            return new AIAnalysisResponse(
                    technicalAnalysis.getSymbol(),
                    technicalAnalysis.getCurrentPrice(),
                    aiResponse.getRecommendation(),
                    aiResponse.getReasoning(),
                    aiResponse.getProbability(),
                    aiResponse.getRiskLevel(),
                    aiResponse.getAnalysis(),
                    aiResponse.getHoldingPeriod(),
                    aiResponse.getInvalidationCondition()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse or validate AI analysis response",
                    e
            );
        }
    }

    private void validateAIResponse(AIAnalysisResponse response) {

        if (response == null) {
            throw new IllegalStateException(
                    "AI returned an empty response"
            );
        }

        if (response.getRecommendation() == null
                || !Set.of("BUY", "HOLD", "SELL")
                .contains(
                        response.getRecommendation()
                                .trim()
                                .toUpperCase()
                )) {
            throw new IllegalStateException(
                    "AI returned an invalid recommendation"
            );
        }

        if (response.getRiskLevel() == null
                || !Set.of("LOW", "MEDIUM", "HIGH")
                .contains(
                        response.getRiskLevel()
                                .trim()
                                .toUpperCase()
                )) {
            throw new IllegalStateException(
                    "AI returned an invalid risk level"
            );
        }

        if (response.getHoldingPeriod() == null
                || !Set.of(
                "SHORT_TERM",
                "MEDIUM_TERM",
                "LONG_TERM"
        ).contains(
                response.getHoldingPeriod()
                        .trim()
                        .toUpperCase()
        )) {
            throw new IllegalStateException(
                    "AI returned an invalid holding period"
            );
        }

        if (!Double.isFinite(response.getProbability())
                || response.getProbability() < 0
                || response.getProbability() > 100) {
            throw new IllegalStateException(
                    "AI confidence score must be between 0 and 100"
            );
        }

        if (response.getReasoning() == null
                || response.getReasoning().isBlank()
                || response.getAnalysis() == null
                || response.getAnalysis().isBlank()
                || response.getInvalidationCondition() == null
                || response.getInvalidationCondition().isBlank()) {
            throw new IllegalStateException(
                    "AI response is missing required analysis fields"
            );
        }
    }
}
