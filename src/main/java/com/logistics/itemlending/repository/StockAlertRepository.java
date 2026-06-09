package com.logistics.itemlending.repository;

import com.logistics.itemlending.entity.StockAlert;
import com.logistics.itemlending.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long>, JpaSpecificationExecutor<StockAlert> {

    List<StockAlert> findByAlertType(AlertType alertType);

    List<StockAlert> findByItemId(Long itemId);

    List<StockAlert> findByRelatedUserId(Long userId);

    List<StockAlert> findByReadFalse();

    List<StockAlert> findByReadFalseOrderByCreateTimeDesc();

    long countByReadFalse();

    boolean existsByAlertTypeAndItemIdAndReadFalse(AlertType alertType, Long itemId);
}
