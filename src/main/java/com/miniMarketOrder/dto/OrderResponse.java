package com.miniMarketOrder.dto;

import java.math.BigDecimal;

public record OrderResponse(String orderId, BigDecimal executedPrice) {

}
