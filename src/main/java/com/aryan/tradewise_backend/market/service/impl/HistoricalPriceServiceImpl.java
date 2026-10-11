package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.config.TwelveDataConfig;
import com.aryan.tradewise_backend.market.dto.HistoricalPriceResponse;
import com.aryan.tradewise_backend.market.service.HistoricalPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class HistoricalPriceServiceImpl
        implements HistoricalPriceService {

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

            String url =
                    "https://api.twelvedata.com/time_series"
                            + "?symbol=" + encodedSymbol
                            + "&interval=" + interval
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

            HistoricalPriceResponse historicalResponse =
                    objectMapper.readValue(
                            response.body(),
                            HistoricalPriceResponse.class
                    );

            if (historicalResponse.getValues() == null
                    || historicalResponse.getValues().isEmpty()) {
                throw new IllegalStateException(
                        "No historical prices returned for " + symbol
                );
            }

            return historicalResponse;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to retrieve historical prices for " + symbol,
                    e
            );
        }
    }
}