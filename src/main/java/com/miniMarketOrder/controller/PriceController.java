package com.miniMarketOrder.controller;

import com.miniMarketOrder.service.PriceFeedClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/price")
public class PriceController {
    private final PriceFeedClient priceFeedClient;

    public PriceController(PriceFeedClient priceFeedClient) {
        this.priceFeedClient = priceFeedClient;
    }

    @GetMapping("/price")
    public ResponseEntity<PriceFeedClient.PriceResponse> getPrice(@RequestParam String symbol) {
        return ResponseEntity.ok(priceFeedClient.getPriceWithSymbol(symbol));
    }

}


