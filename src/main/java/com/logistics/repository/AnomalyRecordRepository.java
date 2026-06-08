package com.logistics.repository;

import com.logistics.entity.AnomalyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface AnomalyRecordRepository extends JpaRepository<AnomalyRecord, Long>, JpaSpecificationExecutor<AnomalyRecord> {
    List<AnomalyRecord> findByItemId(Long itemId);
    List<AnomalyRecord> findByBorrowRecordId(Long borrowRecordId);
    List<AnomalyRecord> findByReporterAndStatus(String reporter, String status);
}
