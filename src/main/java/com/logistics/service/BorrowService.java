package com.logistics.service;

import com.logistics.dto.*;
import com.logistics.entity.BorrowRecord;
import com.logistics.entity.Item;
import com.logistics.repository.BorrowRecordRepository;
import com.logistics.repository.ItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public BorrowRecord apply(BorrowRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        if (item.getAvailableQty() < request.getQty()) {
            throw new RuntimeException("库存不足，当前可用数量: " + item.getAvailableQty());
        }
        BorrowRecord record = new BorrowRecord();
        record.setItemId(request.getItemId());
        record.setBorrowerName(request.getBorrowerName());
        record.setBorrowerDept(request.getBorrowerDept());
        record.setQty(request.getQty());
        record.setStatus("APPLIED");
        record.setBorrowDate(LocalDate.now());
        record.setExpectedReturnDate(request.getExpectedReturnDate());
        record.setRemark(request.getRemark());
        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord approve(Long id, BorrowApproveRequest request) {
        BorrowRecord record = findById(id);
        if (!"APPLIED".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许审批");
        }
        Item item = itemRepository.findById(record.getItemId())
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        if (item.getAvailableQty() < record.getQty()) {
            throw new RuntimeException("库存不足，当前可用数量: " + item.getAvailableQty());
        }
        item.setAvailableQty(item.getAvailableQty() - record.getQty());
        itemRepository.save(item);
        record.setStatus("APPROVED");
        record.setApprover(request.getApprover());
        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord returnItem(Long id, BorrowReturnRequest request) {
        BorrowRecord record = findById(id);
        if (!"APPROVED".equals(record.getStatus()) && !"OVERDUE".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许归还");
        }
        Item item = itemRepository.findById(record.getItemId())
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        item.setAvailableQty(item.getAvailableQty() + record.getQty());
        itemRepository.save(item);
        record.setStatus("RETURNED");
        record.setActualReturnDate(request.getActualReturnDate() != null ? request.getActualReturnDate() : LocalDate.now());
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord markAnomaly(Long id, BorrowAnomalyRequest request) {
        BorrowRecord record = findById(id);
        if (!"APPROVED".equals(record.getStatus()) && !"OVERDUE".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许标记异常");
        }
        record.setAnomalyRemark(request.getAnomalyRemark());
        return borrowRecordRepository.save(record);
    }

    @Transactional
    public void checkAndUpdateOverdue() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(LocalDate.now());
        for (BorrowRecord record : overdueRecords) {
            if ("APPROVED".equals(record.getStatus())) {
                record.setStatus("OVERDUE");
                borrowRecordRepository.save(record);
            }
        }
    }

    public BorrowRecord findById(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
    }

    public List<BorrowRecord> query(BorrowQueryRequest request) {
        List<Long> filterItemIds = null;
        if (request.getCategoryId() != null || request.getLocationId() != null) {
            List<Item> filteredItems = itemRepository.findAll().stream()
                    .filter(i -> request.getCategoryId() == null || request.getCategoryId().equals(i.getCategoryId()))
                    .filter(i -> request.getLocationId() == null || request.getLocationId().equals(i.getLocationId()))
                    .collect(Collectors.toList());
            filterItemIds = filteredItems.stream().map(Item::getId).collect(Collectors.toList());
            if (filterItemIds.isEmpty()) {
                return List.of();
            }
        }
        List<Long> finalFilterItemIds = filterItemIds;
        Specification<BorrowRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getItemId() != null) {
                predicates.add(cb.equal(root.get("itemId"), request.getItemId()));
            }
            if (finalFilterItemIds != null) {
                predicates.add(root.get("itemId").in(finalFilterItemIds));
            }
            if (request.getBorrowerName() != null && !request.getBorrowerName().isEmpty()) {
                predicates.add(cb.like(root.get("borrowerName"), "%" + request.getBorrowerName() + "%"));
            }
            if (request.getBorrowerDept() != null && !request.getBorrowerDept().isEmpty()) {
                predicates.add(cb.like(root.get("borrowerDept"), "%" + request.getBorrowerDept() + "%"));
            }
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getApprover() != null && !request.getApprover().isEmpty()) {
                predicates.add(cb.like(root.get("approver"), "%" + request.getApprover() + "%"));
            }
            if (request.getHasAnomaly() != null && request.getHasAnomaly()) {
                predicates.add(cb.isNotNull(root.get("anomalyRemark")));
                predicates.add(cb.notEqual(root.get("anomalyRemark"), ""));
            }
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowDate"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("borrowDate"), request.getEndDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return borrowRecordRepository.findAll(spec);
    }

    public List<Object[]> getOverdueRanking() {
        return borrowRecordRepository.findOverdueRanking();
    }

    public boolean hasConsecutiveAnomalyReturn(String responsiblePerson, int threshold) {
        List<Item> items = itemRepository.findAll().stream()
                .filter(i -> responsiblePerson.equals(i.getResponsiblePerson()))
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            return false;
        }
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<BorrowRecord> records = borrowRecordRepository.findByItemIdInAndStatus(itemIds, "RETURNED");
        int consecutiveCount = 0;
        for (BorrowRecord record : records) {
            if (record.getAnomalyRemark() != null && !record.getAnomalyRemark().isEmpty()) {
                consecutiveCount++;
                if (consecutiveCount >= threshold) {
                    return true;
                }
            } else {
                consecutiveCount = 0;
            }
        }
        return false;
    }
}
