package com.aryan.tradewise_backend.portfolio.service.impl;

import com.aryan.tradewise_backend.portfolio.dto.PortfolioResponse;
import com.aryan.tradewise_backend.portfolio.entity.Portfolio;
import com.aryan.tradewise_backend.portfolio.repository.PortfolioRepository;
import com.aryan.tradewise_backend.portfolio.service.PortfolioService;
import com.aryan.tradewise_backend.security.CurrentUserService;
import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public PortfolioServiceImpl(
            PortfolioRepository portfolioRepository,
            CurrentUserService currentUserService,
            UserRepository userRepository) {

        this.portfolioRepository = portfolioRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Override
    public List<PortfolioResponse> getMyPortfolio() {

        String email = currentUserService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Portfolio> portfolios = portfolioRepository.findByUser(user);

        return portfolios.stream()
                .map(portfolio -> {

                    BigDecimal currentPrice =
                            portfolio.getAsset().getCurrentPrice();

                    BigDecimal currentValue =
                            portfolio.getQuantity()
                                    .multiply(currentPrice);

                    return PortfolioResponse.builder()
                            .assetSymbol(portfolio.getAsset().getSymbol())
                            .assetName(portfolio.getAsset().getName())
                            .quantity(portfolio.getQuantity())
                            .currentPrice(currentPrice)
                            .currentValue(currentValue)
                            .build();
                })
                .toList();
    }
}