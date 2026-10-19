
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

    private static final Logger log =
            LoggerFactory.getLogger(AIAnalysisServiceImpl.class);

    private static final String FINANCIAL_DISCLAIMER =
            "This analysis is for educational paper-trading purposes only "
                    + "and is not financial advice. AI confidence is subjective "
                    + "and does not represent the probability of profit. "
                    + "Market conditions can change, and losses are possible.";

    private static final Set<String> VALID_RECOMMENDATIONS =
            Set.of("BUY", "HOLD", "SELL");

    private static final Set<String> VALID_RISK_LEVELS =
            Set.of("LOW", "MEDIUM", "HIGH");

    private static final Set<String> VALID_HOLDING_PERIODS =
            Set.of("SHORT_TERM", "MEDIUM_TERM", "LONG_TERM");

    private static final int MAX_REASONING_LENGTH = 2000;
    private static final int MAX_ANALYSIS_LENGTH = 5000;
    private static final int MAX_INVALIDATION_LENGTH = 1000;
    private static final double LOW_CONFIDENCE_THRESHOLD = 40.0;

    private final OpenAIClient openAIClient;

    public AIAnalysisServiceImpl(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @Override
    public AIAnalysisResponse analyzeStock(
            TechnicalAnalysisResponse technicalAnalysis) {

        validateTechnicalAnalysis(technicalAnalysis);

        String symbol = technicalAnalysis.getSymbol()
                .trim()
                .toUpperCase(Locale.ROOT);

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
                - A null value means the data is unavailable, not zero.
                - Do not invent missing prices, indicators, historical data, or news.
                - Do not claim to have accessed news or other sources that were not provided.
                - Distinguish observed market data from your interpretation.
                - If historical indicators are unavailable, explicitly acknowledge this limitation.
                - If evidence is insufficient, prefer HOLD and explain why.

                Analysis rules:
                - recommendation must be BUY, HOLD, or SELL.
                - riskLevel must be LOW, MEDIUM, or HIGH.
                - holdingPeriod must be SHORT_TERM, MEDIUM_TERM, or LONG_TERM.
                - probability must be between 0 and 100.
                - probability is subjective model confidence, NOT the probability of profit.
                - Do not treat subjective confidence as a statistically validated prediction.
                - Do not invent precise entry prices, price targets, or stop-losses.
                - Explain a condition that could invalidate the analysis.
                - Never guarantee returns.
                - This is educational paper-trading analysis, not financial advice.
                """.formatted(
                symbol,
                technicalAnalysis.getCurrentPrice(),
                technicalAnalysis.getSma20(),
                technicalAnalysis.getSma50(),
                technicalAnalysis.getPriceChangePercent(),
                technicalAnalysis.getTrend()
        );

        try {
            StructuredResponseCreateParams<AIModelAnalysis> params =
                    ResponseCreateParams.builder()
                            .model("gpt-5-mini")
                            .input(prompt)
                            .text(AIModelAnalysis.class)
                            .build();

            var response = openAIClient.responses().create(params);

            // Reject responses that did not finish successfully.
            String responseStatus = response.status()
                    .map(Object::toString)
                    .orElse("unknown");

            if (!"completed".equalsIgnoreCase(responseStatus)) {
                throw new AIServiceException(
                        "OpenAI did not complete the analysis",
                        new IllegalStateException(
                                "OpenAI response status: " + responseStatus
                        )
                );
            }

            // Extract the structured model output.
            AIModelAnalysis aiResponse = response.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .findFirst()
                    .orElseThrow(() ->
                            new AIServiceException(
                                    "OpenAI returned no analysis",
                                    new IllegalStateException(
                                            "No structured analysis was returned"
                                    )
                            )
                    );

            validateAIResponse(aiResponse);

            // Normalize enum-like fields.
            aiResponse.setRecommendation(
                    aiResponse.getRecommendation()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

            aiResponse.setRiskLevel(
                    aiResponse.getRiskLevel()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

            aiResponse.setHoldingPeriod(
                    aiResponse.getHoldingPeriod()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

            // Conservative product heuristic, not a validated trading strategy.
            if (aiResponse.getProbability() < LOW_CONFIDENCE_THRESHOLD
                    && !"HOLD".equals(aiResponse.getRecommendation())) {

                aiResponse.setRecommendation("HOLD");

                String confidenceNote =
                        " Recommendation changed to HOLD because the model's "
                                + "subjective confidence score was below 40.";

                String reasoning = aiResponse.getReasoning();

                int availableLength =
                        MAX_REASONING_LENGTH - confidenceNote.length();

                if (reasoning.length() + confidenceNote.length()
                        <= MAX_REASONING_LENGTH) {

                    aiResponse.setReasoning(reasoning + confidenceNote);

                } else {
                    aiResponse.setReasoning(
                            reasoning.substring(0, availableLength)
                                    + confidenceNote
                    );
                }
            }

            // Identity, price, timestamp, and disclaimer remain backend-controlled.
            return new AIAnalysisResponse(
                    symbol,
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

        } catch (AIServiceException exception) {
            log.error(
                    "AI analysis failed for symbol {}",
                    symbol,
                    exception
            );
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Unexpected failure generating AI analysis for symbol {}",
                    symbol,
                    exception
            );

            throw new AIServiceException(
                    "Failed to generate a valid AI stock analysis",
                    exception
            );
        }
    }

    private void validateTechnicalAnalysis(
            TechnicalAnalysisResponse technicalAnalysis) {

        if (technicalAnalysis == null) {
            throw new AIServiceException(
                    "Technical analysis data is required",
                    new IllegalArgumentException(
                            "technicalAnalysis must not be null"
                    )
            );
        }

        if (technicalAnalysis.getSymbol() == null
                || technicalAnalysis.getSymbol().isBlank()) {
            throw new AIServiceException(
                    "A valid stock symbol is required",
                    new IllegalArgumentException(
                            "symbol must not be blank"
                    )
            );
        }

        if (technicalAnalysis.getCurrentPrice() == null
                || technicalAnalysis.getCurrentPrice().signum() <= 0) {
            throw new AIServiceException(
                    "A valid current market price is required",
                    new IllegalArgumentException(
                            "currentPrice must be positive"
                    )
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
                || !VALID_RECOMMENDATIONS.contains(
                response.getRecommendation().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "AI returned an invalid recommendation"
            );
        }

        if (response.getRiskLevel() == null
                || !VALID_RISK_LEVELS.contains(
                response.getRiskLevel().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "AI returned an invalid risk level"
            );
        }

        if (response.getHoldingPeriod() == null
                || !VALID_HOLDING_PERIODS.contains(
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

        if (response.getReasoning().length() > MAX_REASONING_LENGTH
                || response.getAnalysis().length() > MAX_ANALYSIS_LENGTH
                || response.getInvalidationCondition().length()
                > MAX_INVALIDATION_LENGTH) {
            throw new IllegalStateException(
                    "AI response contains text exceeding the allowed limits"
            );
        }
    }
}
