package com.logistics.lending.service;

import com.logistics.lending.entity.Item;
import com.logistics.lending.entity.LendingRecord;
import com.logistics.lending.entity.MaintenanceRecord;
import com.logistics.lending.repository.ItemRepository;
import com.logistics.lending.repository.LendingRecordRepository;
import com.logistics.lending.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ItemRepository itemRepository;
    private final LendingRecordRepository lendingRecordRepository;
    private final MaintenanceRecordRepository maintenanceRepository;

    @Value("${app.warning.stock-threshold:5}")
    private int stockThreshold;

    @Value("${app.warning.overdue-days:7}")
    private int overdueDays;

    @Value("${app.warning.abnormal-count-threshold:3}")
    private int abnormalCountThreshold;

    public List<Item> getLowStockList() {
        return itemRepository.findLowStockItems();
    }

    public List<LendingRecord> getOverdueList() {
        return lendingRecordRepository.findAllOverdueRecords(LocalDateTime.now());
    }

    public List<LendingRecord> getCurrentlyOverdueList() {
        return lendingRecordRepository.findOverdueRecords(LocalDateTime.now());
    }

    public List<Map<String, Object>> getOverdueRanking() {
        List<Object[]> results = lendingRecordRepository.findOverdueBorrowersRanking(LocalDateTime.now());
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("borrowerName", row[0]);
            item.put("overdueCount", row[1]);
            ranking.add(item);
        }
        return ranking;
    }

    public List<MaintenanceRecord> getMaintenancePendingReview() {
        return maintenanceRepository.findPendingReviewRecords();
    }

    public List<LendingRecord> getUnconfirmedAbnormal() {
        return lendingRecordRepository.findUnconfirmedAbnormalRecords();
    }

    public List<Map<String, Object>> getFrequentAbnormalBorrowers() {
        List<LendingRecord> records = lendingRecordRepository.findAll();
        Map<String, Long> borrowerAbnormalCount = new HashMap<>();
        for (LendingRecord r : records) {
            if (r.getAbnormalType() != null) {
                borrowerAbnormalCount.merge(r.getBorrowerName(), 1L, Long::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        borrowerAbnormalCount.forEach((name, count) -> {
            if (count >= abnormalCountThreshold) {
                Map<String, Object> m = new HashMap<>();
                m.put("borrowerName", name);
                m.put("abnormalCount", count);
                result.add(m);
            }
        });
        result.sort((a, b) -> Long.compare((Long) b.get("abnormalCount"), (Long) a.get("abnormalCount")));
        return result;
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("lowStockCount", getLowStockList().size());
        dashboard.put("overdueCount", getOverdueList().size());
        dashboard.put("pendingMaintenanceReviewCount", getMaintenancePendingReview().size());
        dashboard.put("unconfirmedAbnormalCount", getUnconfirmedAbnormal().size());
        dashboard.put("frequentAbnormalBorrowers", getFrequentAbnormalBorrowers());
        return dashboard;
    }
}
