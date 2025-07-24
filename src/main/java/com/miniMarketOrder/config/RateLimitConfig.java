package com.miniMarketOrder.config;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitConfig implements Filter {
    private static final int LIMIT = 10;
    private static final long INTERVAL_MS = 1000;
    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String accountId = request.getParameter("accountId");
        if (accountId == null) {
            chain.doFilter(request, response);
            return;
        }

        RequestCounter counter = counters.computeIfAbsent(accountId, k -> new RequestCounter());
        synchronized (counter) {
            long now = System.currentTimeMillis();
            if (now - counter.timestamp > INTERVAL_MS) {
                counter.timestamp = now;
                counter.count.set(0);
            }
            if (counter.count.incrementAndGet() > LIMIT) {
                response.getWriter().write("Rate limit exceeded");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static class RequestCounter {
        long timestamp = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}