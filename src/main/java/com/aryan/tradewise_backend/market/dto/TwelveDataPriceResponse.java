package com.aryan.tradewise_backend.market.dto;

import lombok.Data;

@Data
public class TwelveDataPriceResponse {

    private String event;
    private String symbol;
    private Double price;
    private Long timestamp;
}