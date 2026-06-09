package com.logistics.lending.service;

import com.logistics.lending.dto.SupplementApproveRequest;
import com.logistics.lending.dto.SupplementRequest;
import com.logistics.lending.entity.Item;
import com.logistics.lending.entity.StockSupplement;
import com.logistics.lending.entity.User;
import com.logistics.lending.exception.BusinessException;
import com.logistics.lending.repository.ItemRepository;
import com.logistics.lending.repository.StockSupplementRepository;
import com.logistics.lending.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplementService {

    private final StockSupplementRepository supplementRepository;
    private final ItemRepository itemRepository;
    private final CurrentUserUtil currentUserUtil;

    @Transactional
    public StockSupplement submit(SupplementRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("物品不存在"));

        User currentUser = currentUserUtil.getCurrentUser();
        StockSupplement record = new StockSupplement();
        record.setItem(item);
        record.setQuantity(request.getQuantity());
        record.setReason(request.getReason());
        record.setStatus("PENDING");
        record.setSubmittedBy(currentUser);
        record.setSubmittedAt(LocalDateTime.now());
        return supplementRepository.save(record);
    }

    @Transactional
    public StockSupplement approve(Long id, SupplementApproveRequest request) {
        StockSupplement record = supplementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("补充申请不存在"));

        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException("该申请状态不允许审批");
        }

        User currentUser = currentUserUtil.getCurrentUser();
        if (Boolean.TRUE.equals(request.getApproved())) {
            Item item = record.getItem();
            item.setTotalQuantity(item.getTotalQuantity() + record.getQuantity());
            item.setAvailableQuantity(item.getAvailableQuantity() + record.getQuantity());
            itemRepository.save(item);
            record.setStatus("APPROVED");
        } else {
            record.setStatus("REJECTED");
        }

        record.setApprovedBy(currentUser);
        record.setApprovedAt(LocalDateTime.now());
        record.setApproveRemark(request.getApproveRemark());
        return supplementRepository.save(record);
    }

    public List<StockSupplement> list(String status) {
        if (status != null && !status.isBlank()) {
            return supplementRepository.findByStatus(status);
        }
        return supplementRepository.findAll();
    }

    public StockSupplement getById(Long id) {
        return supplementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("补充申请不存在"));
    }
}
