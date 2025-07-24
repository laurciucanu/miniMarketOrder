package com.miniMarketOrder.dto;

import com.miniMarketOrder.utils.Side;

public record OrderRequest(String accountId, String symbol, Side side, int quantity) {

}