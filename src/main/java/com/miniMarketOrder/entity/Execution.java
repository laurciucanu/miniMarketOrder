package com.miniMarketOrder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "executions")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Execution {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Order order;

    private BigDecimal price;

    private LocalDateTime executedAt;

    public Execution(Order order, BigDecimal price) {
        this.order = order;
        this.price = price;
    }
}