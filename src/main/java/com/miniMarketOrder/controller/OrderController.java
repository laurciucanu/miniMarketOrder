package com.miniMarketOrder.controller;

import com.miniMarketOrder.dto.OrderRequest;
import com.miniMarketOrder.dto.OrderResponse;
import com.miniMarketOrder.entity.Order;
import com.miniMarketOrder.service.OrderService;
import com.miniMarketOrder.service.PriceFeedClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestBody OrderRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(service.createOrder(req, idempotencyKey));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable String id) {
        return ResponseEntity.ok(service.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<List<Order>> list(@RequestParam String accountId) {
        return ResponseEntity.ok(service.listOrders(accountId));
    }

    @GetMapping("/price")
    public ResponseEntity<PriceFeedClient.PriceResponse> getPrice(@RequestParam String symbol) {
        return ResponseEntity.ok(service.getPrice(symbol));
    }

}

