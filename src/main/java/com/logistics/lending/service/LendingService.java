package com.logistics.lending.service;

import com.logistics.lending.dto.LendingRequest;
import com.logistics.lending.dto.ReturnRequest;
import com.logistics.lending.entity.Item;
import com.logistics.lending.entity.LendingRecord;
import com.logistics.lending.entity.MaintenanceRecord;
import com.logistics.lending.entity.User;
import com.logistics.lending.exception.BusinessException;
import com.logistics.lending.repository.ItemRepository;
import com.logistics.lending.repository.LendingRecordRepository;
import com.logistics.lending.repository.MaintenanceRecordRepository;
import com.logistics.lending.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LendingService {

    private final LendingRecordRepository lendingRecordRepository;
    private final ItemRepository itemRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final CurrentUserUtil currentUserUtil;

    @Transactional
    public LendingRecord applyLending(LendingRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("物品不存在"));

        if (item.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessException("库存不足，当前可用数量: " + item.getAvailableQuantity());
        }

        LendingRecord record = new LendingRecord();
        record.setItem(item);
        record.setBorrowerName(request.getBorrowerName());
        record.setBorrowerDept(request.getBorrowerDept());
        record.setBorrowerPhone(request.getBorrowerPhone());
        record.setQuantity(request.getQuantity());
        record.setStatus("PENDING");
        record.setPurpose(request.getPurpose());
        record.setExpectedReturnDate(request.getExpectedReturnDate());
        return lendingRecordRepository.save(record);
    }

    @Transactional
    public LendingRecord confirmLending(Long recordId) {
        LendingRecord record = lendingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("借用记录不存在"));

        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException("该记录状态不允许借出确认");
        }

        Item item = record.getItem();
        if (item.getAvailableQuantity() < record.getQuantity()) {
            throw new BusinessException("库存不足，无法借出");
        }

        User currentUser = currentUserUtil.getCurrentUser();
        item.setAvailableQuantity(item.getAvailableQuantity() - record.getQuantity());
        item.setLentQuantity(item.getLentQuantity() + record.getQuantity());
        itemRepository.save(item);

        record.setStatus("LENT");
        record.setLentAt(LocalDateTime.now());
        record.setLentBy(currentUser);
        return lendingRecordRepository.save(record);
    }

    @Transactional
    public LendingRecord returnItem(Long recordId, ReturnRequest request) {
        LendingRecord record = lendingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("借用记录不存在"));

        if (!"LENT".equals(record.getStatus())) {
            throw new BusinessException("该记录状态不允许归还");
        }

        User currentUser = currentUserUtil.getCurrentUser();
        Item item = record.getItem();

        item.setLentQuantity(item.getLentQuantity() - record.getQuantity());

        boolean isAbnormal = request.getAbnormalType() != null && !request.getAbnormalType().isBlank();
        if (isAbnormal) {
            item.setMaintenanceQuantity(item.getMaintenanceQuantity() + record.getQuantity());
            record.setStatus("ABNORMAL");
        } else {
            item.setAvailableQuantity(item.getAvailableQuantity() + record.getQuantity());
            record.setStatus("RETURNED");
        }
        itemRepository.save(item);

        record.setReturnedAt(LocalDateTime.now());
        record.setReceivedBy(currentUser);
        record.setReturnRemark(request.getReturnRemark());
        record.setAbnormalType(request.getAbnormalType());
        record.setAbnormalRemark(request.getAbnormalRemark());
        LendingRecord savedRecord = lendingRecordRepository.save(record);

        if (isAbnormal) {
            MaintenanceRecord m = new MaintenanceRecord();
            m.setItem(item);
            m.setLendingRecord(savedRecord);
            m.setQuantity(record.getQuantity());
            m.setStatus("IN_MAINTENANCE");
            m.setProblemDescription("异常归还: " + (request.getAbnormalRemark() != null ? request.getAbnormalRemark() : request.getAbnormalType()));
            m.setSubmittedBy(currentUser);
            m.setSubmittedAt(LocalDateTime.now());
            maintenanceRecordRepository.save(m);
        }

        return savedRecord;
    }

    @Transactional
    public LendingRecord confirmAbnormal(Long recordId) {
        LendingRecord record = lendingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("借用记录不存在"));

        if (!"ABNORMAL".equals(record.getStatus())) {
            throw new BusinessException("该记录状态不是异常状态");
        }

        User currentUser = currentUserUtil.getCurrentUser();
        record.setAbnormalConfirmed(true);
        record.setAbnormalConfirmedBy(currentUser);
        record.setAbnormalConfirmedAt(LocalDateTime.now());
        record.setStatus("ABNORMAL_CONFIRMED");
        return lendingRecordRepository.save(record);
    }

    public List<LendingRecord> search(Long categoryId, Long locationId, String borrowerName,
                                       String status, String abnormalType,
                                       LocalDateTime startDate, LocalDateTime endDate) {
        return lendingRecordRepository.searchRecords(categoryId, locationId, borrowerName,
                status, abnormalType, startDate, endDate);
    }

    public LendingRecord getById(Long id) {
        return lendingRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("借用记录不存在"));
    }
}
