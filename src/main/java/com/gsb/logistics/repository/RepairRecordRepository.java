package com.gsb.logistics.repository;

import com.gsb.logistics.domain.RepairRecord;
import com.gsb.logistics.domain.RepairStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RepairRecordRepository extends JpaRepository<RepairRecord, Long>, JpaSpecificationExecutor<RepairRecord> {
    List<RepairRecord> findByStatus(RepairStatus status);
}
