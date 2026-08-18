package com.aryan.tradewise_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class WalletResponse {

    private Long id;
    private BigDecimal dailyLimit;
    private BigDecimal availableBalance;
    private BigDecimal lockedBalance;
    private String currency;
}