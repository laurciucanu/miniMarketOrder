package com.miniMarketOrder.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class IdempotencyKey {
    @Id
    private String key;
    private LocalDateTime createdAt;
}
