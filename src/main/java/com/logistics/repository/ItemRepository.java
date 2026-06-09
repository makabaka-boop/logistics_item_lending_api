package com.logistics.repository;

import com.logistics.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByCategoryId(Long categoryId);
    List<Item> findByLocationId(Long locationId);
    List<Item> findByAvailableQtyLessThanEqual(Integer threshold);
    List<Item> findByStatus(String status);
}
