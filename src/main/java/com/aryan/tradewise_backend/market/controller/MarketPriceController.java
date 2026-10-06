package com.aryan.tradewise_backend.market.controller;

import com.aryan.tradewise_backend.market.service.MarketPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/market-price")
public class MarketPriceController {

    private final MarketPriceService marketPriceService;

    public MarketPriceController(MarketPriceService marketPriceService) {
        this.marketPriceService = marketPriceService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<BigDecimal> getMarketPrice(
            @PathVariable String symbol) {

        BigDecimal price = marketPriceService.getPrice(symbol);

        if (price == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(price);
    }
}