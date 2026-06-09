package com.logistics.repository;

import com.logistics.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {
    List<BorrowRecord> findByItemIdAndStatusIn(Long itemId, List<String> statuses);

    @Query("SELECT b.borrowerName, COUNT(b) as cnt FROM BorrowRecord b WHERE b.status = 'OVERDUE' GROUP BY b.borrowerName ORDER BY cnt DESC")
    List<Object[]> findOverdueRanking();

    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'APPROVED' AND b.expectedReturnDate < :date")
    List<BorrowRecord> findOverdueRecords(@Param("date") LocalDate date);

    List<BorrowRecord> findByBorrowerNameAndStatus(String borrowerName, String status);

    List<BorrowRecord> findByItemIdInAndStatus(List<Long> itemIds, String status);
}
