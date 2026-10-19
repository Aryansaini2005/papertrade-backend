package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.config.TwelveDataConfig;
import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;
import com.aryan.tradewise_backend.market.service.HistoricalPriceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
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
                    .timeout(Duration.ofSeconds(15))
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

            JsonNode root = objectMapper.readTree(response.body());

            if ("error".equalsIgnoreCase(
                    root.path("status").asText())) {

                throw new IllegalStateException(
                        "Twelve Data error: "
                                + root.path("message")
                                .asText("Unknown provider error")
                );
            }

            JsonNode values = root.path("values");

            if (!values.isArray() || values.isEmpty()) {
                throw new IllegalStateException(
                        "No historical prices returned for " + symbol
                );
            }

            HistoricalPriceResponse history =
                    objectMapper.treeToValue(
                            root,
                            HistoricalPriceResponse.class
                    );

            if (history.getValues() == null
                    || history.getValues().isEmpty()) {

                throw new IllegalStateException(
                        "Historical response contains no candles for "
                                + symbol
                );
            }

            for (HistoricalPriceResponse.Candle candle
                    : history.getValues()) {

                if (candle == null
                        || candle.getDatetime() == null
                        || candle.getDatetime().isBlank()) {

                    throw new IllegalStateException(
                            "Historical response contains a candle "
                                    + "without a datetime for " + symbol
                    );
                }

                validatePositivePrice(
                        candle.getOpen(), "open", symbol);

                validatePositivePrice(
                        candle.getHigh(), "high", symbol);

                validatePositivePrice(
                        candle.getLow(), "low", symbol);

                validatePositivePrice(
                        candle.getClose(), "close", symbol);
            }

            return history;

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

    private void validatePositivePrice(
            String value,
            String field,
            String symbol) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Historical candle has no " + field
                            + " price for " + symbol
            );
        }

        try {
            if (new BigDecimal(value).signum() <= 0) {
                throw new IllegalStateException(
                        "Historical candle has an invalid " + field
                                + " price for " + symbol
                );
            }

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Historical candle has a malformed " + field
                            + " price for " + symbol,
                    e
            );
        }
    }
}
