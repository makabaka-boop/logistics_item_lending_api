package com.logistics.itemlending.repository;

import com.logistics.itemlending.entity.Item;
import com.logistics.itemlending.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByItemCode(String itemCode);

    boolean existsByItemCode(String itemCode);

    List<Item> findByCategoryId(Long categoryId);

    List<Item> findByLocationId(Long locationId);

    List<Item> findByResponsiblePersonId(Long responsiblePersonId);

    List<Item> findByStatus(ItemStatus status);

    List<Item> findByEnabledTrue();

    @Query("SELECT i FROM Item i WHERE i.availableQuantity <= i.warningThreshold AND i.enabled = true")
    List<Item> findLowStockItems();
}
