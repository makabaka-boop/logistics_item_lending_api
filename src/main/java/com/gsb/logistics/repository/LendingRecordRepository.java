package com.gsb.logistics.repository;

import com.gsb.logistics.domain.LendingRecord;
import com.gsb.logistics.domain.LendingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LendingRecordRepository extends JpaRepository<LendingRecord, Long>, JpaSpecificationExecutor<LendingRecord> {

    List<LendingRecord> findTop20ByBorrowerOrderByApplyTimeDesc(String borrower);

    List<LendingRecord> findByStatus(LendingStatus status);
}
