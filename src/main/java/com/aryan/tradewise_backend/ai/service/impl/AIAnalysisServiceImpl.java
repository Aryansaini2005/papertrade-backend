package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.dto.TechnicalAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Service;

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

                Analyze the following market data:

                Symbol: %s
                Current price: %s
                SMA 20: %s
                SMA 50: %s
                Price change percentage: %s
                Trend: %s

                Return ONLY valid JSON with these fields:
                {
                  "recommendation": "BUY, HOLD, or SELL",
                  "reasoning": "Explain the evidence and limitations",
                  "probability": 0,
                  "riskLevel": "LOW, MEDIUM, or HIGH",
                  "analysis": "Detailed educational analysis"
                }

                Rules:
                - probability must be between 0 and 100.
                - It represents subjective confidence in the recommendation,
                  NOT the probability of profit.
                - Treat missing indicators as unavailable, not zero.
                - Use only the supplied data; do not invent news or prices.
                - Explain how the indicators support or contradict the
                  recommendation.
                - A trend indicator alone is not enough to guarantee
                  a profitable trade.
                - Clearly explain uncertainty and relevant risks.
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

        try {
            AIAnalysisResponse aiResponse =
                    objectMapper.readValue(
                            json,
                            AIAnalysisResponse.class
                    );

            return new AIAnalysisResponse(
                    technicalAnalysis.getSymbol(),
                    technicalAnalysis.getCurrentPrice(),
                    aiResponse.getRecommendation(),
                    aiResponse.getReasoning(),
                    aiResponse.getProbability(),
                    aiResponse.getRiskLevel(),
                    aiResponse.getAnalysis()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse AI analysis response",
                    e
            );
        }
    }
}
