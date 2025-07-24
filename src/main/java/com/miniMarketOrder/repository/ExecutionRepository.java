package com.miniMarketOrder.repository;

import com.miniMarketOrder.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, Long> {
    List<Execution> findByStatus(String status);
    List<Execution> findByOrderId(Long orderId);
    List<Execution> findByExecutionTimeBetween(LocalDateTime start, LocalDateTime end);
}
