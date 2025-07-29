package com.miniMarketOrder.service;

import com.miniMarketOrder.dto.OrderRequest;
import com.miniMarketOrder.dto.OrderResponse;
import com.miniMarketOrder.entity.Execution;
import com.miniMarketOrder.entity.IdempotencyKey;
import com.miniMarketOrder.entity.Order;
import com.miniMarketOrder.metrics.OrderMetrics;
import com.miniMarketOrder.repository.ExecutionRepository;
import com.miniMarketOrder.repository.IdempotencyKeyRepository;
import com.miniMarketOrder.repository.OrderRepository;
import com.miniMarketOrder.utils.Status;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.miniMarketOrder.service.PriceFeedClient.*;

@Service
public class OrderService {
    private final OrderRepository orderRepo;
    private final ExecutionRepository execRepo;
    private final PriceFeedClient priceFeedClient;

    @Autowired
    public IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private OrderMetrics orderMetrics;


    public OrderService(OrderRepository orderRepo, ExecutionRepository execRepo, PriceFeedClient priceFeedClient) {
        this.orderRepo = orderRepo;
        this.execRepo = execRepo;
        this.priceFeedClient = priceFeedClient;
    }

    @Transactional
    public OrderResponse processOrder(OrderRequest request) {
        if (request.quantity() <= 0 || (!"BUY".equalsIgnoreCase(String.valueOf(request.side())) && !"SELL".equalsIgnoreCase(String.valueOf(request.side())))) {
            throw new IllegalArgumentException("Invalid quantity or side");
        }

        PriceResponse priceResponse;
        try {
            priceResponse = fetchPriceWithRetry(request.symbol());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("5xx")) {
                throw new ServiceUnavailableException("Price feed service unavailable");
            } else {
                throw new UnprocessableEntityException("Price feed error: " + e.getMessage());
            }
        }

        if (priceResponse.price() == null) {
            throw new UnprocessableEntityException("Price feed unavailable for symbol: " + request.symbol());
        }

        Order order = new Order(UUID.randomUUID().toString(), request.accountId(), request.symbol(), request.side(), request.quantity(), Status.CONFIRMED, LocalDateTime.now());

        Order savedOrder = orderRepo.save(order);
        execRepo.save(new Execution(savedOrder, priceResponse.price().setScale(6, RoundingMode.HALF_UP)));

        return new OrderResponse(savedOrder.getId(), priceResponse.price());
    }

    public OrderResponse createOrder(OrderRequest request, String idempotencyKey) {
        if (idempotencyKeyRepository.existsById(idempotencyKey)) {
            throw new IllegalArgumentException("Duplicate request with the same idempotency key");
        }

        OrderResponse response = processOrder(request);

        IdempotencyKey key = new IdempotencyKey();
        key.setKey(idempotencyKey);
        key.setCreatedAt(LocalDateTime.now());
        idempotencyKeyRepository.save(key);

        orderMetrics.incrementOrderCounter();

        return response;
    }

    public PriceResponse fetchPriceWithRetry(String symbol) {
        try {
            return priceFeedClient.getPriceWithSymbol(symbol);
        } catch (RuntimeException e) {
            if (isServerError(e)) {
                return priceFeedClient.getPriceWithSymbol(symbol);
            }
            throw e;
        }
    }

    private boolean isServerError(RuntimeException e) {
        return e.getMessage().contains("5xx");
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class UnprocessableEntityException extends RuntimeException {
        public UnprocessableEntityException(String message) {
            super(message);
        }
    }

    public Order getOrder(String id) {
        return orderRepo.findById(id).orElseThrow();
    }

    public PriceResponse getPrice(String symbol) {
        return priceFeedClient.getPriceWithSymbol(symbol);
    }

    public List<Order> listOrders(String accountId) {
        return orderRepo.findByAccountId(accountId);
    }
}

