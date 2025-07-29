package com.miniMarketOrder.controller;

import com.miniMarketOrder.service.PriceFeedClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Price API", description = "Endpoints for retrieving price information")
@RestController
@RequestMapping("/price")
public class PriceController {
    private final PriceFeedClient priceFeedClient;

    public PriceController(PriceFeedClient priceFeedClient) {
        this.priceFeedClient = priceFeedClient;
    }

    @Operation(summary = "Get price by symbol", description = "Returns the price for the given symbol")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PriceFeedClient.PriceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid symbol provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Symbol not found",
                    content = @Content)
    })
    @GetMapping("/price")
    public ResponseEntity<PriceFeedClient.PriceResponse> getPrice(
            @Parameter(description = "Symbol to get the price for", required = true)
            @RequestParam String symbol) {
        return ResponseEntity.ok(priceFeedClient.getPriceWithSymbol(symbol));
    }

}


