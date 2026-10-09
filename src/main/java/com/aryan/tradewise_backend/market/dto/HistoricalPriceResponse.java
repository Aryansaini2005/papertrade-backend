package com.aryan.tradewise_backend.market.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HistoricalPriceResponse {

    private String symbol;

    @JsonProperty("meta")
    private Meta meta;

    @JsonProperty("values")
    private List<Candle> values;

    @Data
    public static class Meta {
        private String symbol;
        private String interval;
    }

    @Data
    public static class Candle {
        private String datetime;
        private String open;
        private String high;
        private String low;
        private String close;
        private String volume;
    }
}
