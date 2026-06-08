package com.logistics.service;

import com.logistics.dto.AnomalyConfirmRequest;
import com.logistics.dto.AnomalyQueryRequest;
import com.logistics.dto.AnomalyRequest;
import com.logistics.entity.AnomalyRecord;
import com.logistics.repository.AnomalyRecordRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyService {

    private final AnomalyRecordRepository anomalyRecordRepository;

    @Transactional
    public AnomalyRecord create(AnomalyRequest request) {
        AnomalyRecord record = new AnomalyRecord();
        record.setItemId(request.getItemId());
        record.setBorrowRecordId(request.getBorrowRecordId());
        record.setType(request.getType());
        record.setDescription(request.getDescription());
        record.setReporter(request.getReporter());
        record.setStatus("REPORTED");
        return anomalyRecordRepository.save(record);
    }

    @Transactional
    public AnomalyRecord confirm(Long id, AnomalyConfirmRequest request) {
        AnomalyRecord record = findById(id);
        if (!"REPORTED".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许确认");
        }
        record.setStatus(request.getStatus() != null ? request.getStatus() : "CONFIRMED");
        return anomalyRecordRepository.save(record);
    }

    public AnomalyRecord findById(Long id) {
        return anomalyRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("异常记录不存在"));
    }

    public List<AnomalyRecord> query(AnomalyQueryRequest request) {
        Specification<AnomalyRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getItemId() != null) {
                predicates.add(cb.equal(root.get("itemId"), request.getItemId()));
            }
            if (request.getBorrowRecordId() != null) {
                predicates.add(cb.equal(root.get("borrowRecordId"), request.getBorrowRecordId()));
            }
            if (request.getType() != null && !request.getType().isEmpty()) {
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getReporter() != null && !request.getReporter().isEmpty()) {
                predicates.add(cb.like(root.get("reporter"), "%" + request.getReporter() + "%"));
            }
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate().atStartOfDay()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate().atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return anomalyRecordRepository.findAll(spec);
    }
}
