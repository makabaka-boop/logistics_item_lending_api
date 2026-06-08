package com.logistics.repository;

import com.logistics.entity.RepairRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface RepairRecordRepository extends JpaRepository<RepairRecord, Long>, JpaSpecificationExecutor<RepairRecord> {
    List<RepairRecord> findByStatus(String status);
    List<RepairRecord> findByItemId(Long itemId);
}
