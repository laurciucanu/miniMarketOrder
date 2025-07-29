package com.miniMarketOrder;

import com.miniMarketOrder.dto.OrderRequest;
import com.miniMarketOrder.entity.Order;
import com.miniMarketOrder.repository.ExecutionRepository;
import com.miniMarketOrder.repository.IdempotencyKeyRepository;
import com.miniMarketOrder.repository.OrderRepository;
import com.miniMarketOrder.service.OrderService;
import com.miniMarketOrder.service.PriceFeedClient;
import com.miniMarketOrder.utils.Side;
import com.miniMarketOrder.utils.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
class MiniMarketOrderApplicationTests {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ExecutionRepository execRepository;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createOrder_shouldReturnSuccess() throws Exception {
		String orderJson = Files.readString(Paths.get("src/test/resources/orderRequest.json"));
		String expectedAccountId = "account12345";
		String expectedSymbol = "AAPL";
		Side expectedSide = Side.BUY;
		int expectedQuantity = 10;
		Status expectedStatus = Status.CONFIRMED;

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderJson))
				.andExpect(status().isOk());

		Order savedOrder = orderRepository.findById(expectedAccountId).orElse(null);
		Assertions.assertNotNull(savedOrder);
		assertEquals(expectedAccountId, savedOrder.getAccountId());
		assertEquals(expectedSymbol, savedOrder.getSymbol());
		assertEquals(expectedSide, savedOrder.getSide());
		assertEquals(expectedQuantity, savedOrder.getQuantity());
		assertEquals(expectedStatus, savedOrder.getStatus());
	}

	@Test
	void getOrder_shouldReturnOrderDetails() throws Exception {
		String expectedId = "0d482267-fdd3-49aa-990d-766e3a08cad2";
		String expectedAccountId = "account12345";
		String expectedSymbol = "AAPL";
		Side expectedSide = Side.BUY;
		int expectedQuantity = 10;
		Status expectedStatus = Status.CONFIRMED;

		Order order = new Order(expectedId, expectedAccountId, expectedSymbol, expectedSide, expectedQuantity, expectedStatus, LocalDateTime.now());
		orderRepository.save(order);

		mockMvc.perform((RequestBuilder) get("/orders/" + expectedId))
				.andExpect(status().isOk());

		assertEquals(expectedId, order.getId());
		assertEquals(expectedAccountId, order.getAccountId());
		assertEquals(expectedSymbol, order.getSymbol());
		assertEquals(expectedSide, order.getSide());
		assertEquals(expectedQuantity, order.getQuantity());
		assertEquals(expectedStatus, order.getStatus());
	}

	@Test
	void getOrdersByAccountId_shouldReturnOrders() throws Exception {
		String expectedId = "1f209cb3-10e7-456b-89cf-23448dba7c03";
		String expectedAccountId = "account12345";
		String expectedSymbol = "AAPL";
		Side expectedSide = Side.BUY;
		int expectedQuantity = 10;
		Status expectedStatus = Status.CONFIRMED;

		Order order = new Order(expectedId, expectedAccountId, expectedSymbol, expectedSide, expectedQuantity, expectedStatus, LocalDateTime.now());
		orderRepository.save(order);

		mockMvc.perform((RequestBuilder) get("/orders?accountId=" + expectedAccountId))
				.andExpect(status().isOk());

		assertEquals(expectedId, order.getId());
		assertEquals(expectedAccountId, order.getAccountId());
		assertEquals(expectedSymbol, order.getSymbol());
		assertEquals(expectedSide, order.getSide());
		assertEquals(expectedQuantity, order.getQuantity());
		assertEquals(expectedStatus, order.getStatus());
	}

	@Test
	void createOrder_withNegativeQuantity_shouldReturnBadRequest() throws Exception {
		String orderJson = Files.readString(Paths.get("src/test/resources/orderBadRequestNegativeQuantity.json"));

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createOrder_withNullQuantity_shouldReturnBadRequest() throws Exception {
		String orderJson = Files.readString(Paths.get("src/test/resources/orderBadRequestNullQuantity.json"));

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderJson))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void createOrder_withServerError_shouldReturnInternalServerError() throws Exception {
		String orderJson = Files.readString(Paths.get("src/test/resources/orderServerError.json"));

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderJson))
				.andExpect(status().is5xxServerError());
	}

	@Test
	void createOrder_withUnprocessableEntity_shouldReturnUnprocessableEntity() throws Exception {
		String orderJson = Files.readString(Paths.get("src/test/resources/orderUnprocessableEntity.json"));

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderJson))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void createOrder_shouldThrowException_whenIdempotencyKeyExists() {
		IdempotencyKeyRepository idempotencyKeyRepository = mock(IdempotencyKeyRepository.class);
		when(idempotencyKeyRepository.existsById("key123")).thenReturn(true);

		OrderService orderService = new OrderService(
				mock(OrderRepository.class),
				mock(ExecutionRepository.class),
				mock(PriceFeedClient.class)
		);
		orderService.idempotencyKeyRepository = idempotencyKeyRepository;

		OrderRequest request = mock(OrderRequest.class);

		assertThrows(IllegalArgumentException.class, () -> {
			orderService.createOrder(request, "key123");
		});
	}

	@Test
	void testFetchPriceWithRetry_ServerError_RetriesOnce() {
		PriceFeedClient mockClient = mock(PriceFeedClient.class);
		String symbol = "AAPL";
		RuntimeException serverError = new RuntimeException("5xx error");
		PriceFeedClient.PriceResponse expectedResponse = new PriceFeedClient.PriceResponse(java.math.BigDecimal.TEN);

		org.mockito.Mockito.when(mockClient.getPriceWithSymbol(symbol))
				.thenThrow(serverError)
				.thenReturn(expectedResponse);

		OrderService service = new OrderService(orderRepository, execRepository, mockClient);

		PriceFeedClient.PriceResponse response = service.fetchPriceWithRetry(symbol);

		assertEquals(expectedResponse.price(), response.price());
	}

}
