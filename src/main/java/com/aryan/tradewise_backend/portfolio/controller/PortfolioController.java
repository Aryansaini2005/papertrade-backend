package com.aryan.tradewise_backend.portfolio.controller;

import com.aryan.tradewise_backend.portfolio.dto.PortfolioResponse;
import com.aryan.tradewise_backend.portfolio.service.PortfolioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public List<PortfolioResponse> getMyPortfolio() {
        return portfolioService.getMyPortfolio();
    }
}