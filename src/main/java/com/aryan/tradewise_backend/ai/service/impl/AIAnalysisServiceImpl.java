package com.aryan.tradewise_backend.ai.service.impl;

import com.aryan.tradewise_backend.ai.dto.AIAnalysisResponse;
import com.aryan.tradewise_backend.ai.service.AIAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
            String symbol,
            BigDecimal currentPrice) {

        String prompt = """
                You are the AI analysis assistant for TradeWise,
                an educational paper-trading platform.

                Stock symbol: %s
                Current market price: %s

                Available evidence is limited to this symbol and price.
                Do not invent historical data, news, technical indicators,
                or financial facts.

                Return ONLY valid JSON with these fields:
                {
                  "recommendation": "BUY, HOLD, or SELL",
                  "reasoning": "Explain the evidence and its limitations",
                  "probability": 0,
                  "riskLevel": "LOW, MEDIUM, or HIGH",
                  "analysis": "Detailed educational analysis"
                }

                Rules:
                - probability must be a number from 0 to 100.
                - probability means your subjective confidence in the
                  recommendation, NOT the probability of profit.
                - Because only one price is available, explicitly mention
                  that there is insufficient evidence to predict price
                  direction reliably.
                - Do not imply that a recommendation is statistically
                  validated.
                - Do not guarantee returns.
                - This is educational paper-trading analysis, not
                  financial advice.
                """.formatted(symbol, currentPrice);

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
                    symbol,
                    currentPrice,
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