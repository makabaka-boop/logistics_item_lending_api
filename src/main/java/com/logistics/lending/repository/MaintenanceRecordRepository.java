package com.logistics.lending.repository;

import com.logistics.lending.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord> findByStatus(String status);

    List<MaintenanceRecord> findByItemId(Long itemId);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.status = 'COMPLETED_PENDING_REVIEW'")
    List<MaintenanceRecord> findPendingReviewRecords();
}
