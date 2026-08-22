package com.aryan.tradewise_backend.market.dto;

import com.aryan.tradewise_backend.market.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class AssetResponse {

    private Long id;
    private String symbol;
    private String name;
    private String exchange;
    private AssetType type;
    private BigDecimal currentPrice;
    private boolean active;
}