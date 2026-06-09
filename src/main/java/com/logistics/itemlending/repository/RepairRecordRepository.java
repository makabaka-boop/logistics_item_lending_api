package com.logistics.itemlending.repository;

import com.logistics.itemlending.entity.RepairRecord;
import com.logistics.itemlending.enums.RepairStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRecordRepository extends JpaRepository<RepairRecord, Long>, JpaSpecificationExecutor<RepairRecord> {

    Optional<RepairRecord> findByRecordNo(String recordNo);

    boolean existsByRecordNo(String recordNo);

    List<RepairRecord> findByItemId(Long itemId);

    List<RepairRecord> findByReporterId(Long reporterId);

    List<RepairRecord> findByRepairerId(Long repairerId);

    List<RepairRecord> findByStatus(RepairStatus status);

    List<RepairRecord> findByStatusOrderByCreateTimeDesc(RepairStatus status);
}
