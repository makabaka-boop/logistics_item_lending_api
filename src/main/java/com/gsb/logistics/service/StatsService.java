package com.gsb.logistics.service;

import com.gsb.logistics.config.AppProperties;
import com.gsb.logistics.domain.*;
import com.gsb.logistics.repository.ItemRepository;
import com.gsb.logistics.repository.LendingRecordRepository;
import com.gsb.logistics.repository.RepairRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private final ItemRepository itemRepo;
    private final LendingRecordRepository lendingRepo;
    private final RepairRecordRepository repairRepo;
    private final AppProperties props;

    public StatsService(ItemRepository itemRepo, LendingRecordRepository lendingRepo,
                        RepairRecordRepository repairRepo, AppProperties props) {
        this.itemRepo = itemRepo;
        this.lendingRepo = lendingRepo;
        this.repairRepo = repairRepo;
        this.props = props;
    }

    /** 库存不足清单 */
    public List<Map<String, Object>> lowStock() {
        double ratio = props.getRules().getLowStockRatio();
        List<Map<String, Object>> ret = new ArrayList<>();
        for (Item it : itemRepo.findAll()) {
            if (it.getTotalQuantity() == 0) continue;
            double r = (double) it.getAvailableQuantity() / it.getTotalQuantity();
            if (r <= ratio) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("itemId", it.getId());
                m.put("name", it.getName());
                m.put("code", it.getCode());
                m.put("total", it.getTotalQuantity());
                m.put("available", it.getAvailableQuantity());
                m.put("ratio", Math.round(r * 100.0) / 100.0);
                m.put("owner", it.getOwner());
                ret.add(m);
            }
        }
        ret.sort(Comparator.comparing(m -> ((Number) m.get("ratio")).doubleValue()));
        return ret;
    }

    /** 逾期归还排行（按借用人维度统计逾期次数） */
    public List<Map<String, Object>> overdueRanking() {
        List<LendingRecord> all = lendingRepo.findAll();
        Map<String, Long> count = all.stream()
                .filter(r -> r.getAbnormalType() == AbnormalType.OVERDUE
                        || r.getStatus() == LendingStatus.OVERDUE)
                .collect(Collectors.groupingBy(LendingRecord::getBorrower, Collectors.counting()));

        List<Map<String, Object>> ret = new ArrayList<>();
        count.forEach((borrower, c) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("borrower", borrower);
            m.put("overdueCount", c);
            ret.add(m);
        });
        ret.sort((a, b) -> Long.compare(((Number) b.get("overdueCount")).longValue(),
                ((Number) a.get("overdueCount")).longValue()));
        return ret;
    }

    /** 维修待复核列表 */
    public List<Map<String, Object>> repairPendingReview() {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (RepairRecord r : repairRepo.findByStatus(RepairStatus.DONE_PENDING_REVIEW)) {
            Item it = itemRepo.findById(r.getItemId()).orElse(null);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("repairId", r.getId());
            m.put("itemId", r.getItemId());
            m.put("itemName", it == null ? null : it.getName());
            m.put("quantity", r.getQuantity());
            m.put("finishTime", r.getFinishTime());
            m.put("issueDescription", r.getIssueDescription());
            m.put("repairRemark", r.getRepairRemark());
            m.put("reviewOverdue", Boolean.TRUE.equals(r.getReviewOverdue()));
            ret.add(m);
        }
        return ret;
    }

    /** 维修完成后未复核超时（异常）清单 */
    public List<Map<String, Object>> repairReviewOverdue() {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (RepairRecord r : repairRepo.findByStatus(RepairStatus.DONE_PENDING_REVIEW)) {
            if (!Boolean.TRUE.equals(r.getReviewOverdue())) continue;
            Item it = itemRepo.findById(r.getItemId()).orElse(null);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("repairId", r.getId());
            m.put("itemId", r.getItemId());
            m.put("itemName", it == null ? null : it.getName());
            m.put("quantity", r.getQuantity());
            m.put("finishTime", r.getFinishTime());
            ret.add(m);
        }
        return ret;
    }

    /** 概览数据 */
    public Map<String, Object> overview() {
        long lent = lendingRepo.findByStatus(LendingStatus.LENT).size();
        long overdue = lendingRepo.findByStatus(LendingStatus.OVERDUE).size();
        long abnormal = lendingRepo.findByStatus(LendingStatus.ABNORMAL).size();
        long pendingReview = repairRepo.findByStatus(RepairStatus.DONE_PENDING_REVIEW).size();
        long reviewOverdue = repairReviewOverdue().size();
        long lowStock = lowStock().size();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalItems", itemRepo.count());
        m.put("lent", lent);
        m.put("overdue", overdue);
        m.put("abnormal", abnormal);
        m.put("repairPendingReview", pendingReview);
        m.put("repairReviewOverdue", reviewOverdue);
        m.put("lowStockCount", lowStock);
        m.put("generatedAt", LocalDateTime.now());
        return m;
    }
}
