package com.miniMarketOrder.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class PriceFeedClient {
    private final RestTemplate restTemplate;

    public PriceFeedClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public PriceResponse getPriceWithSymbol(String symbol) throws RuntimeException {
        try {
            var response = restTemplate.getForEntity("http://mock-price-feed:8081/price?symbol=" + symbol, PriceResponse.class);
            return Optional.ofNullable(response.getBody()).orElseThrow();
        } catch (HttpServerErrorException ex) {
            throw new RuntimeException("Price feed unavailable", ex);
        }
    }

    public record PriceResponse(BigDecimal price) { }
}
