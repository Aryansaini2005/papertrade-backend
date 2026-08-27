package com.aryan.tradewise_backend.market.dto;

import com.aryan.tradewise_backend.market.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateAssetRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Exchange is required")
    private String exchange;

    @NotNull(message = "Asset type is required")
    private AssetType type;

    @NotNull(message = "Current price is required")
    @Positive(message = "Current price must be greater than zero")
    private BigDecimal currentPrice;
}