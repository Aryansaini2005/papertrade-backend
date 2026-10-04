package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.config.TwelveDataConfig;
import com.aryan.tradewise_backend.market.dto.TwelveDataPriceResponse;
import com.aryan.tradewise_backend.market.service.TwelveDataWebSocketService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

@Service
public class TwelveDataWebSocketServiceImpl
        implements TwelveDataWebSocketService {

    private final TwelveDataConfig config;
    private final ObjectMapper objectMapper;

    public TwelveDataWebSocketServiceImpl(
            TwelveDataConfig config,
            ObjectMapper objectMapper) {

        this.config = config;
        this.objectMapper = objectMapper;

        System.out.println("TwelveDataWebSocketServiceImpl CREATED");
    }

    @PostConstruct
    public void start() {
        System.out.println("Starting Twelve Data WebSocket...");
        connect();
    }

    @Override
    public void connect() {

        System.out.println(
                "Environment variable loaded: "
                        + (System.getenv("TWELVE_DATA_API_KEY") != null)
        );

        System.out.println(
                "Spring config loaded: "
                        + (config.getApiKey() != null
                        && !config.getApiKey().isBlank())
        );

        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create(
                "wss://ws.twelvedata.com/v1/quotes/price?apikey="
                        + config.getApiKey()
        );

        client.newWebSocketBuilder()
                .buildAsync(uri, new WebSocket.Listener() {

                    @Override
                    public void onOpen(WebSocket webSocket) {

                        System.out.println("Connected to Twelve Data WebSocket");

                        webSocket.request(1);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        String subscribeMessage = """
            {
                "action": "subscribe",
                "params": {
                    "symbols": "BTC/USD"
                }
            }
            """;

                        webSocket.sendText(
                                subscribeMessage,
                                true
                        );
                    }

                    @Override
                    public CompletionStage<?> onText(
                            WebSocket webSocket,
                            CharSequence data,
                            boolean last) {

                        try {

                            TwelveDataPriceResponse response =
                                    objectMapper.readValue(
                                            data.toString(),
                                            TwelveDataPriceResponse.class
                                    );

                            if ("price".equals(response.getEvent())) {

                                System.out.println(
                                        "Symbol: " + response.getSymbol()
                                );

                                System.out.println(
                                        "Price: " + response.getPrice()
                                );

                                System.out.println(
                                        "Timestamp: " + response.getTimestamp()
                                );
                            }

                        } catch (Exception e) {

                            System.err.println(
                                    "Failed to parse Twelve Data message: "
                                            + e.getMessage()
                            );
                        }

                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public void onError(
                            WebSocket webSocket,
                            Throwable error) {

                        System.err.println(
                                "WebSocket error: "
                                        + error.getMessage()
                        );
                    }
                })
                .whenComplete((webSocket, error) -> {

                    if (error != null) {

                        System.err.println(
                                "WebSocket connection failed: "
                                        + error.getMessage()
                        );

                    } else {

                        System.out.println(
                                "WebSocket connection established successfully"
                        );
                    }
                });
    }
}