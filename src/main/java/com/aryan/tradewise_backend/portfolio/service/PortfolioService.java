package com.aryan.tradewise_backend.portfolio.service;

import com.aryan.tradewise_backend.portfolio.dto.PortfolioResponse;

import java.util.List;

public interface PortfolioService {

    List<PortfolioResponse> getMyPortfolio();
}