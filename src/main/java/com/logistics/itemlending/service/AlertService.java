package com.logistics.itemlending.service;

import com.logistics.itemlending.entity.StockAlert;
import com.logistics.itemlending.enums.AlertType;
import com.logistics.itemlending.repository.StockAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private StockAlertRepository stockAlertRepository;

    public StockAlert createAlert(StockAlert alert) {
        return stockAlertRepository.save(alert);
    }

    public StockAlert getAlertById(Long id) {
        return stockAlertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("预警记录不存在"));
    }

    public StockAlert markAsRead(Long id) {
        StockAlert alert = getAlertById(id);
        alert.setRead(true);
        alert.setReadTime(LocalDateTime.now());
        return stockAlertRepository.save(alert);
    }

    public void markAllAsRead() {
        List<StockAlert> unreadAlerts = stockAlertRepository.findByReadFalse();
        for (StockAlert alert : unreadAlerts) {
            alert.setRead(true);
            alert.setReadTime(LocalDateTime.now());
        }
        stockAlertRepository.saveAll(unreadAlerts);
    }

    public Page<StockAlert> listAlerts(AlertType alertType, Boolean read, Pageable pageable) {
        Specification<StockAlert> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (alertType != null) {
                predicates.add(cb.equal(root.get("alertType"), alertType));
            }

            if (read != null) {
                predicates.add(cb.equal(root.get("read"), read));
            }

            query.orderBy(cb.desc(root.get("createTime")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return stockAlertRepository.findAll(spec, pageable);
    }

    public List<StockAlert> listUnreadAlerts() {
        return stockAlertRepository.findByReadFalseOrderByCreateTimeDesc();
    }

    public long countUnreadAlerts() {
        return stockAlertRepository.countByReadFalse();
    }

    public boolean existsStockAlert(AlertType alertType, Long itemId) {
        return stockAlertRepository.existsByAlertTypeAndItemIdAndReadFalse(alertType, itemId);
    }

    public void deleteAlert(Long id) {
        if (!stockAlertRepository.existsById(id)) {
            throw new EntityNotFoundException("预警记录不存在");
        }
        stockAlertRepository.deleteById(id);
    }
}
