package com.logistics.lending.repository;

import com.logistics.lending.entity.StockSupplement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockSupplementRepository extends JpaRepository<StockSupplement, Long> {
    List<StockSupplement> findByStatus(String status);
    List<StockSupplement> findByItemId(Long itemId);
}
