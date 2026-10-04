package com.aryan.tradewise_backend.market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "twelvedata")
public class TwelveDataConfig {

    private String apiKey;
}