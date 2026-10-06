package com.aryan.tradewise_backend.admin.controller;

import com.aryan.tradewise_backend.trade.entity.Trade;
import com.aryan.tradewise_backend.trade.repository.TradeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trades")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTradeController {

    private final TradeRepository tradeRepository;

    public AdminTradeController(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @GetMapping
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }
}