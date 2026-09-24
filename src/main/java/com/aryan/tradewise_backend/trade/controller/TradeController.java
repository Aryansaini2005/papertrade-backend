package com.aryan.tradewise_backend.trade.controller;

import com.aryan.tradewise_backend.trade.dto.TradeResponse;
import com.aryan.tradewise_backend.trade.service.TradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping
    public List<TradeResponse> getMyTrades() {
        return tradeService.getMyTrades();
    }
}