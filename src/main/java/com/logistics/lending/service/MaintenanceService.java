package com.logistics.lending.service;

import com.logistics.lending.dto.MaintenanceCompleteRequest;
import com.logistics.lending.dto.MaintenanceRequest;
import com.logistics.lending.dto.MaintenanceReviewRequest;
import com.logistics.lending.entity.Item;
import com.logistics.lending.entity.MaintenanceRecord;
import com.logistics.lending.entity.User;
import com.logistics.lending.exception.BusinessException;
import com.logistics.lending.repository.ItemRepository;
import com.logistics.lending.repository.MaintenanceRecordRepository;
import com.logistics.lending.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRepository;
    private final ItemRepository itemRepository;
    private final CurrentUserUtil currentUserUtil;

    @Transactional
    public MaintenanceRecord submit(MaintenanceRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("物品不存在"));

        if (item.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessException("可用数量不足，无法提交维修");
        }

        User currentUser = currentUserUtil.getCurrentUser();
        item.setAvailableQuantity(item.getAvailableQuantity() - request.getQuantity());
        item.setMaintenanceQuantity(item.getMaintenanceQuantity() + request.getQuantity());
        itemRepository.save(item);

        MaintenanceRecord record = new MaintenanceRecord();
        record.setItem(item);
        record.setQuantity(request.getQuantity());
        record.setStatus("IN_MAINTENANCE");
        record.setProblemDescription(request.getProblemDescription());
        record.setSubmittedBy(currentUser);
        record.setSubmittedAt(LocalDateTime.now());
        return maintenanceRepository.save(record);
    }

    @Transactional
    public MaintenanceRecord complete(Long id, MaintenanceCompleteRequest request) {
        MaintenanceRecord record = maintenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维修记录不存在"));

        if (!"IN_MAINTENANCE".equals(record.getStatus())) {
            throw new BusinessException("该记录状态不允许完成维修");
        }

        record.setStatus("COMPLETED_PENDING_REVIEW");
        if (request != null) {
            record.setMaintenanceResult(request.getMaintenanceResult());
        }
        return maintenanceRepository.save(record);
    }

    @Transactional
    public MaintenanceRecord review(Long id, MaintenanceReviewRequest request) {
        MaintenanceRecord record = maintenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维修记录不存在"));

        if (!"COMPLETED_PENDING_REVIEW".equals(record.getStatus())) {
            throw new BusinessException("该记录状态不允许复核");
        }

        User currentUser = currentUserUtil.getCurrentUser();
        Item item = record.getItem();

        boolean approved = request != null && Boolean.TRUE.equals(request.getApproved());
        if (approved) {
            item.setMaintenanceQuantity(item.getMaintenanceQuantity() - record.getQuantity());
            item.setAvailableQuantity(item.getAvailableQuantity() + record.getQuantity());
            record.setStatus("REVIEWED_PASSED");
        } else {
            item.setMaintenanceQuantity(item.getMaintenanceQuantity() - record.getQuantity());
            item.setScrappedQuantity(item.getScrappedQuantity() + record.getQuantity());
            record.setStatus("REVIEWED_REJECTED");
        }
        itemRepository.save(item);

        record.setReviewedBy(currentUser);
        record.setReviewedAt(LocalDateTime.now());
        if (request != null) {
            record.setReviewRemark(request.getReviewRemark());
        }
        return maintenanceRepository.save(record);
    }

    public List<MaintenanceRecord> list(String status) {
        if (status != null && !status.isBlank()) {
            return maintenanceRepository.findByStatus(status);
        }
        return maintenanceRepository.findAll();
    }

    public List<MaintenanceRecord> listPendingReview() {
        return maintenanceRepository.findPendingReviewRecords();
    }

    public MaintenanceRecord getById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维修记录不存在"));
    }
}
