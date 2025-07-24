package com.miniMarketOrder.entity;
import com.miniMarketOrder.utils.Side;
import com.miniMarketOrder.utils.Status;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.*;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String accountId;
    private String symbol;

    @Enumerated(EnumType.STRING)
    private Side side;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
}
