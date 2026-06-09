package com.logistics.lending.repository;

import com.logistics.lending.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsByItemCode(String itemCode);

    @Query("SELECT i FROM Item i WHERE i.availableQuantity <= i.warningThreshold")
    List<Item> findLowStockItems();

    List<Item> findByCategoryId(Long categoryId);
    List<Item> findByLocationId(Long locationId);
}
