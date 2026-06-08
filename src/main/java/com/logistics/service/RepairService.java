package com.logistics.service;

import com.logistics.dto.RepairActionRequest;
import com.logistics.dto.RepairQueryRequest;
import com.logistics.dto.RepairRequest;
import com.logistics.dto.RepairReviewRequest;
import com.logistics.entity.Item;
import com.logistics.entity.RepairRecord;
import com.logistics.repository.ItemRepository;
import com.logistics.repository.RepairRecordRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRecordRepository repairRecordRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public RepairRecord create(RepairRequest request) {
        RepairRecord record = new RepairRecord();
        record.setItemId(request.getItemId());
        record.setBorrowRecordId(request.getBorrowRecordId());
        record.setDescription(request.getDescription());
        record.setStatus("REPORTED");
        record.setReporter(request.getReporter());
        record.setRepairPerson(request.getRepairPerson());
        record.setRemark(request.getRemark());
        RepairRecord saved = repairRecordRepository.save(record);
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        item.setStatus("REPAIR");
        itemRepository.save(item);
        return saved;
    }

    @Transactional
    public RepairRecord startRepair(Long id, RepairActionRequest request) {
        RepairRecord record = findById(id);
        if (!"REPORTED".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许开始维修");
        }
        record.setStatus("REPAIRING");
        record.setRepairDate(LocalDate.now());
        if (request.getRepairPerson() != null) {
            record.setRepairPerson(request.getRepairPerson());
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        return repairRecordRepository.save(record);
    }

    @Transactional
    public RepairRecord completeRepair(Long id, RepairActionRequest request) {
        RepairRecord record = findById(id);
        if (!"REPAIRING".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许完成维修");
        }
        record.setStatus("COMPLETED");
        record.setCompletedDate(LocalDate.now());
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        return repairRecordRepository.save(record);
    }

    @Transactional
    public RepairRecord review(Long id, RepairReviewRequest request) {
        RepairRecord record = findById(id);
        if (!"COMPLETED".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许复核");
        }
        record.setStatus("REVIEWED");
        record.setReviewedDate(LocalDate.now());
        record.setReviewer(request.getReviewer());
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        RepairRecord saved = repairRecordRepository.save(record);
        Item item = itemRepository.findById(record.getItemId())
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        item.setStatus("NORMAL");
        itemRepository.save(item);
        return saved;
    }

    public RepairRecord findById(Long id) {
        return repairRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("维修记录不存在"));
    }

    public List<RepairRecord> query(RepairQueryRequest request) {
        Specification<RepairRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getItemId() != null) {
                predicates.add(cb.equal(root.get("itemId"), request.getItemId()));
            }
            if (request.getBorrowRecordId() != null) {
                predicates.add(cb.equal(root.get("borrowRecordId"), request.getBorrowRecordId()));
            }
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getReporter() != null && !request.getReporter().isEmpty()) {
                predicates.add(cb.like(root.get("reporter"), "%" + request.getReporter() + "%"));
            }
            if (request.getRepairPerson() != null && !request.getRepairPerson().isEmpty()) {
                predicates.add(cb.like(root.get("repairPerson"), "%" + request.getRepairPerson() + "%"));
            }
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate().atStartOfDay()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate().atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repairRecordRepository.findAll(spec);
    }

    public List<RepairRecord> findPendingReview() {
        return repairRecordRepository.findByStatus("COMPLETED");
    }
}
