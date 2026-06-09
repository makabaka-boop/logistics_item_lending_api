package com.logistics.itemlending.service;

import com.logistics.itemlending.entity.Item;
import com.logistics.itemlending.entity.RepairRecord;
import com.logistics.itemlending.entity.User;
import com.logistics.itemlending.enums.BorrowStatus;
import com.logistics.itemlending.repository.BorrowRecordRepository;
import com.logistics.itemlending.repository.ItemRepository;
import com.logistics.itemlending.repository.RepairRecordRepository;
import com.logistics.itemlending.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private RepairRecordRepository repairRecordRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Item> getLowStockList() {
        return itemRepository.findLowStockItems();
    }

    public List<Map<String, Object>> getOverdueRanking() {
        List<Object[]> results = borrowRecordRepository.findOverdueRanking(
                BorrowStatus.OVERDUE, LocalDateTime.now());

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Object[] result : results) {
            Long userId = (Long) result[0];
            Long count = (Long) result[1];

            Map<String, Object> item = new HashMap<>();
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                item.put("userId", user.getId());
                item.put("username", user.getUsername());
                item.put("realName", user.getRealName());
                item.put("overdueCount", count);
                ranking.add(item);
            }
        }
        return ranking;
    }

    public List<RepairRecord> getRepairPendingReviewList() {
        return repairRecordRepository.findByStatusOrderByCreateTimeDesc(
                com.logistics.itemlending.enums.RepairStatus.REPAIRED);
    }

    public List<Map<String, Object>> getFrequentExceptionUsers() {
        List<Object[]> results = borrowRecordRepository.findFrequentExceptionUsers();

        List<Map<String, Object>> users = new ArrayList<>();
        for (Object[] result : results) {
            Long userId = (Long) result[0];
            Long count = (Long) result[1];

            Map<String, Object> item = new HashMap<>();
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                item.put("userId", user.getId());
                item.put("username", user.getUsername());
                item.put("realName", user.getRealName());
                item.put("exceptionCount", count);
                users.add(item);
            }
        }
        return users;
    }

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalItems = itemRepository.count();
        long lowStockItems = itemRepository.findLowStockItems().size();
        long totalBorrowed = borrowRecordRepository.findByStatus(BorrowStatus.BORROWED).size();
        long overdueCount = borrowRecordRepository.findByStatus(BorrowStatus.OVERDUE).size();
        long repairingCount = repairRecordRepository.findByStatus(
                com.logistics.itemlending.enums.RepairStatus.REPAIRING).size();
        long pendingReviewCount = repairRecordRepository.findByStatus(
                com.logistics.itemlending.enums.RepairStatus.REPAIRED).size();
        long unreadAlerts = 0;

        stats.put("totalItems", totalItems);
        stats.put("lowStockItems", lowStockItems);
        stats.put("totalBorrowed", totalBorrowed);
        stats.put("overdueCount", overdueCount);
        stats.put("repairingCount", repairingCount);
        stats.put("pendingReviewCount", pendingReviewCount);
        stats.put("unreadAlerts", unreadAlerts);

        return stats;
    }
}
