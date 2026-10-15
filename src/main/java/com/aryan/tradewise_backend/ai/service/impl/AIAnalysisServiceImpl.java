package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.dto.AIModelAnalysis;
import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.exception.AIServiceException;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private static final String FINANCIAL_DISCLAIMER =
            "This analysis is for educational paper-trading purposes only "
                    + "and is not financial advice. AI confidence is subjective "
                    + "and does not represent the probability of profit. "
                    + "Market conditions can change, and losses are possible.";

    private static final Logger log =
            LoggerFactory.getLogger(AIAnalysisServiceImpl.class);

    private final OpenAIClient openAIClient;

    public AIAnalysisServiceImpl(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
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
                - A null value means unavailable, not zero.
                - Do not invent missing prices, indicators, or news.
                - Distinguish observed data from interpretations.
                - Explain uncertainty and relevant risks.

                Analysis rules:
                - recommendation must be BUY, HOLD, or SELL.
                - riskLevel must be LOW, MEDIUM, or HIGH.
                - holdingPeriod must be SHORT_TERM, MEDIUM_TERM,
                  or LONG_TERM.
                - probability must be between 0 and 100.
                - probability is subjective model confidence,
                  NOT the probability of profit.
                - If evidence is insufficient, prefer HOLD and explain why.
                - Do not invent precise entry prices, targets, or stop-losses.
                - Explain a condition that could invalidate the analysis.
                - Never guarantee returns.
                - This is educational paper-trading analysis,
                  not financial advice.
                """.formatted(
                technicalAnalysis.getSymbol(),
                technicalAnalysis.getCurrentPrice(),
                technicalAnalysis.getSma20(),
                technicalAnalysis.getSma50(),
                technicalAnalysis.getPriceChangePercent(),
                technicalAnalysis.getTrend()
        );

        // Derive the required JSON schema from AIModelAnalysis.
        StructuredResponseCreateParams<AIModelAnalysis> params =
                ResponseCreateParams.builder()
                        .model("gpt-5-mini")
                        .input(prompt)
                        .text(AIModelAnalysis.class)
                        .build();

        try {
            var response = openAIClient.responses().create(params);

            // The SDK provides structured output as AIModelAnalysis.
            AIModelAnalysis aiResponse = response.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "OpenAI returned no analysis"
                            )
                    );

            validateAIResponse(aiResponse);

            // Normalize enum-like values for consistent API responses.
            aiResponse.setRecommendation(
                    aiResponse.getRecommendation().trim().toUpperCase(Locale.ROOT)
            );

            aiResponse.setRiskLevel(
                    aiResponse.getRiskLevel().trim().toUpperCase(Locale.ROOT)
            );

            aiResponse.setHoldingPeriod(
                    aiResponse.getHoldingPeriod().trim().toUpperCase(Locale.ROOT)
            );


            if (aiResponse.getProbability() < 40
                    && !"HOLD".equals(aiResponse.getRecommendation())) {

                aiResponse.setRecommendation("HOLD");

                aiResponse.setReasoning(
                        aiResponse.getReasoning()
                                + " The recommendation was changed to HOLD because "
                                + "the model's subjective confidence score was below 40."
                );
            }

            // These fields are controlled by our backend.
            return new AIAnalysisResponse(
                    technicalAnalysis.getSymbol(),
                    technicalAnalysis.getCurrentPrice(),
                    aiResponse.getRecommendation(),
                    aiResponse.getReasoning(),
                    aiResponse.getProbability(),
                    aiResponse.getRiskLevel(),
                    aiResponse.getAnalysis(),
                    aiResponse.getHoldingPeriod(),
                    aiResponse.getInvalidationCondition(),
                    FINANCIAL_DISCLAIMER,
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("AI analysis generation failed", e);

            if (e instanceof AIServiceException aiException) {
                throw aiException;
            }

            throw new AIServiceException(
                    "Failed to generate a valid AI stock analysis",
                    e
            );
        }
    }


    private void validateAIResponse(AIModelAnalysis response) {
        if (response == null) {
            throw new IllegalStateException(
                    "AI returned an empty response"
            );
        }

        if (response.getRecommendation() == null
                || !Set.of("BUY", "HOLD", "SELL").contains(
                response.getRecommendation().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "AI returned an invalid recommendation"
            );
        }

        if (response.getRiskLevel() == null
                || !Set.of("LOW", "MEDIUM", "HIGH").contains(
                response.getRiskLevel().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "AI returned an invalid risk level"
            );
        }

        if (response.getHoldingPeriod() == null
                || !Set.of("SHORT_TERM", "MEDIUM_TERM", "LONG_TERM").contains(
                response.getHoldingPeriod().trim().toUpperCase(Locale.ROOT))) {
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
                    "AI response is missing required fields"
            );
        }

        if (response.getReasoning().length() > 2000
                || response.getAnalysis().length() > 5000
                || response.getInvalidationCondition().length() > 1000) {
            throw new IllegalStateException(
                    "AI response contains text exceeding the allowed limits"
            );
        }


    }
}
