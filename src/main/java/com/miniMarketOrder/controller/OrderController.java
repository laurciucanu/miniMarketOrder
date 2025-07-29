package com.miniMarketOrder.controller;

import com.miniMarketOrder.dto.OrderRequest;
import com.miniMarketOrder.dto.OrderResponse;
import com.miniMarketOrder.entity.Order;
import com.miniMarketOrder.service.OrderService;
import com.miniMarketOrder.service.PriceFeedClient;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Order API", description = "Endpoints for managing orders")
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new order",
               description = "Creates a new order with the provided details. Optionally supports idempotency.")

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestBody OrderRequest req,
            @Parameter(description = "Idempotency key to prevent duplicate order creation")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(service.createOrder(req, idempotencyKey));
    }

    @Operation(summary = "Get order by ID",
               description = "Retrieves the order details for the specified order ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Order> get(
            @Parameter(description = "Order ID") @PathVariable String id) {
        return ResponseEntity.ok(service.getOrder(id));
    }

    @Operation(summary = "List orders for an account",
               description = "Returns a list of orders for the specified account ID.")
    @GetMapping
    public ResponseEntity<List<Order>> list(
            @Parameter(description = "Account ID") @RequestParam String accountId) {
        return ResponseEntity.ok(service.listOrders(accountId));
    }

    @Operation(summary = "Get price for a symbol",
               description = "Fetches the current price for the given symbol.")
    @GetMapping("/price")
    public ResponseEntity<PriceFeedClient.PriceResponse> getPrice(
            @Parameter(description = "Symbol to fetch price for") @RequestParam String symbol) {
        return ResponseEntity.ok(service.getPrice(symbol));
    }

}

