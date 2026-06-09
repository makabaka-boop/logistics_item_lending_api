package com.logistics.itemlending.service;

import com.logistics.itemlending.entity.Item;
import com.logistics.itemlending.entity.RepairRecord;
import com.logistics.itemlending.entity.StockAlert;
import com.logistics.itemlending.entity.User;
import com.logistics.itemlending.enums.AlertType;
import com.logistics.itemlending.enums.RepairStatus;
import com.logistics.itemlending.exception.BusinessException;
import com.logistics.itemlending.repository.ItemRepository;
import com.logistics.itemlending.repository.RepairRecordRepository;
import com.logistics.itemlending.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RepairService {

    @Autowired
    private RepairRecordRepository repairRecordRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertService alertService;

    @Transactional
    public RepairRecord submitRepair(Long itemId, Integer quantity, String faultDescription, Long reporterId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("物品不存在"));

        if (quantity <= 0) {
            throw new BusinessException("维修数量必须大于0");
        }

        if (item.getAvailableQuantity() < quantity) {
            throw new BusinessException("可维修数量不足，当前可用数量: " + item.getAvailableQuantity());
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("报修人不存在"));

        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setRepairingQuantity(item.getRepairingQuantity() + quantity);
        itemRepository.save(item);

        RepairRecord record = new RepairRecord();
        record.setRecordNo(generateRecordNo());
        record.setItem(item);
        record.setQuantity(quantity);
        record.setReporter(reporter);
        record.setFaultDescription(faultDescription);
        record.setStatus(RepairStatus.SUBMITTED);

        return repairRecordRepository.save(record);
    }

    @Transactional
    public RepairRecord startRepair(Long recordId, Long repairerId) {
        RepairRecord record = repairRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("维修记录不存在"));

        if (!RepairStatus.SUBMITTED.equals(record.getStatus())) {
            throw new BusinessException("只有已提交的维修记录才能开始维修");
        }

        User repairer = userRepository.findById(repairerId)
                .orElseThrow(() -> new EntityNotFoundException("维修人不存在"));

        record.setStatus(RepairStatus.REPAIRING);
        record.setRepairer(repairer);
        record.setRepairStartTime(LocalDateTime.now());

        return repairRecordRepository.save(record);
    }

    @Transactional
    public RepairRecord finishRepair(Long recordId, String repairDescription) {
        RepairRecord record = repairRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("维修记录不存在"));

        if (!RepairStatus.REPAIRING.equals(record.getStatus())) {
            throw new BusinessException("只有维修中的记录才能完成维修");
        }

        record.setStatus(RepairStatus.REPAIRED);
        record.setRepairDescription(repairDescription);
        record.setRepairFinishTime(LocalDateTime.now());

        createRepairReviewAlert(record);

        return repairRecordRepository.save(record);
    }

    @Transactional
    public RepairRecord reviewRepair(Long recordId, Long reviewerId, String reviewRemark, Boolean passed) {
        RepairRecord record = repairRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("维修记录不存在"));

        if (!RepairStatus.REPAIRED.equals(record.getStatus())) {
            throw new BusinessException("只有已维修完成的记录才能复核");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new EntityNotFoundException("复核人不存在"));

        if (Boolean.TRUE.equals(passed)) {
            Item item = record.getItem();
            item.setAvailableQuantity(item.getAvailableQuantity() + record.getQuantity());
            item.setRepairingQuantity(item.getRepairingQuantity() - record.getQuantity());
            if (item.getRepairingQuantity() < 0) {
                item.setRepairingQuantity(0);
            }
            itemRepository.save(item);

            record.setStatus(RepairStatus.REVIEWED);
        } else {
            record.setStatus(RepairStatus.REPAIRING);
        }

        record.setReviewer(reviewer);
        record.setReviewRemark(reviewRemark);
        record.setReviewTime(LocalDateTime.now());

        return repairRecordRepository.save(record);
    }

    public RepairRecord getRecordById(Long id) {
        return repairRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("维修记录不存在"));
    }

    public Page<RepairRecord> listRecords(Long itemId, Long reporterId, Long repairerId,
                                          RepairStatus status, LocalDateTime startTime,
                                          LocalDateTime endTime, Pageable pageable) {
        Specification<RepairRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (itemId != null) {
                predicates.add(cb.equal(root.get("item").get("id"), itemId));
            }

            if (reporterId != null) {
                predicates.add(cb.equal(root.get("reporter").get("id"), reporterId));
            }

            if (repairerId != null) {
                predicates.add(cb.equal(root.get("repairer").get("id"), repairerId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repairRecordRepository.findAll(spec, pageable);
    }

    public List<RepairRecord> listPendingReviewRecords() {
        return repairRecordRepository.findByStatusOrderByCreateTimeDesc(RepairStatus.REPAIRED);
    }

    public void cancelRepair(Long recordId) {
        RepairRecord record = repairRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("维修记录不存在"));

        if (!RepairStatus.SUBMITTED.equals(record.getStatus())) {
            throw new BusinessException("只有已提交的维修记录才能取消");
        }

        Item item = record.getItem();
        item.setAvailableQuantity(item.getAvailableQuantity() + record.getQuantity());
        item.setRepairingQuantity(item.getRepairingQuantity() - record.getQuantity());
        if (item.getRepairingQuantity() < 0) {
            item.setRepairingQuantity(0);
        }
        itemRepository.save(item);

        record.setStatus(RepairStatus.CANCELLED);
        repairRecordRepository.save(record);
    }

    private void createRepairReviewAlert(RepairRecord record) {
        StockAlert alert = new StockAlert();
        alert.setAlertType(AlertType.REPAIR_PENDING_REVIEW);
        alert.setTitle("维修待复核");
        alert.setContent("物品 " + record.getItem().getName() + " 维修完成，等待复核");
        alert.setItem(record.getItem());
        alertService.createAlert(alert);
    }

    private String generateRecordNo() {
        return "RR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
