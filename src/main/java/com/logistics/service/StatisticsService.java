package com.logistics.service;

import com.logistics.entity.BorrowRecord;
import com.logistics.entity.Item;
import com.logistics.entity.RepairRecord;
import com.logistics.repository.BorrowRecordRepository;
import com.logistics.repository.ItemRepository;
import com.logistics.repository.RepairRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ItemRepository itemRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final RepairRecordRepository repairRecordRepository;
    private final BorrowService borrowService;

    public Map<String, Object> getLowStockList(Integer threshold) {
        Map<String, Object> result = new HashMap<>();
        int t = threshold != null ? threshold : 5;
        List<Item> items = itemRepository.findByAvailableQtyLessThanEqual(t);
        result.put("threshold", t);
        result.put("count", items.size());
        result.put("items", items);
        return result;
    }

    public Map<String, Object> getOverdueRanking() {
        Map<String, Object> result = new HashMap<>();
        List<Object[]> ranking = borrowRecordRepository.findOverdueRanking();
        result.put("ranking", ranking);
        return result;
    }

    public Map<String, Object> getRepairPendingReview() {
        Map<String, Object> result = new HashMap<>();
        List<RepairRecord> records = repairRecordRepository.findByStatus("COMPLETED");
        result.put("count", records.size());
        result.put("records", records);
        return result;
    }

    public Map<String, Object> getConsecutiveAnomaly(String responsiblePerson, Integer threshold) {
        Map<String, Object> result = new HashMap<>();
        int t = threshold != null ? threshold : 3;
        boolean hasConsecutive = borrowService.hasConsecutiveAnomalyReturn(responsiblePerson, t);
        result.put("responsiblePerson", responsiblePerson);
        result.put("threshold", t);
        result.put("hasConsecutiveAnomaly", hasConsecutive);
        return result;
    }
}
