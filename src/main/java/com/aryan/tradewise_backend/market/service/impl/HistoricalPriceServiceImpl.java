package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.config.TwelveDataConfig;
import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;
import com.aryan.tradewise_backend.market.service.HistoricalPriceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class HistoricalPriceServiceImpl implements HistoricalPriceService {

    private final TwelveDataConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public HistoricalPriceServiceImpl(
            TwelveDataConfig config,
            ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public HistoricalPriceResponse getHistoricalPrices(
            String symbol,
            String interval) {

        try {
            String encodedSymbol = URLEncoder.encode(
                    symbol,
                    StandardCharsets.UTF_8
            );

            String encodedInterval = URLEncoder.encode(
                    interval,
                    StandardCharsets.UTF_8
            );

            String url = "https://api.twelvedata.com/time_series"
                    + "?symbol=" + encodedSymbol
                    + "&interval=" + encodedInterval
                    + "&outputsize=60"
                    + "&apikey=" + config.getApiKey();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Twelve Data request failed with HTTP "
                                + response.statusCode()
                );
            }

            // Parse the body first to detect provider-level errors,
            // including errors returned with HTTP 200.
            JsonNode root = objectMapper.readTree(response.body());

            if ("error".equalsIgnoreCase(
                    root.path("status").asText())) {
                String providerMessage =
                        root.path("message").asText("Unknown provider error");

                throw new IllegalStateException(
                        "Twelve Data error: " + providerMessage
                );
            }

            // A valid response should contain historical candle values.
            JsonNode values = root.path("values");

            if (!values.isArray() || values.isEmpty()) {
                throw new IllegalStateException(
                        "No historical prices returned for " + symbol
                );
            }

            return objectMapper.treeToValue(
                    root,
                    HistoricalPriceResponse.class
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Historical price request was interrupted for " + symbol,
                    e
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to retrieve historical prices for " + symbol
                            + ": " + e.getMessage(),
                    e
            );
        }
    }
}
