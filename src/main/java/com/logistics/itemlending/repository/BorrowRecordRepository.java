package com.logistics.itemlending.repository;

import com.logistics.itemlending.entity.BorrowRecord;
import com.logistics.itemlending.enums.BorrowStatus;
import com.logistics.itemlending.enums.ExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {

    Optional<BorrowRecord> findByRecordNo(String recordNo);

    boolean existsByRecordNo(String recordNo);

    List<BorrowRecord> findByItemId(Long itemId);

    List<BorrowRecord> findByBorrowerId(Long borrowerId);

    List<BorrowRecord> findByStatus(BorrowStatus status);

    List<BorrowRecord> findByHasExceptionTrue();

    List<BorrowRecord> findByExceptionType(ExceptionType exceptionType);

    @Query("SELECT b FROM BorrowRecord b WHERE b.status = :status AND b.expectedReturnTime < :now")
    List<BorrowRecord> findOverdueRecords(BorrowStatus status, LocalDateTime now);

    @Query("SELECT b.borrower.id, COUNT(b) as cnt FROM BorrowRecord b WHERE b.status = :status AND b.expectedReturnTime < :now GROUP BY b.borrower.id ORDER BY cnt DESC")
    List<Object[]> findOverdueRanking(BorrowStatus status, LocalDateTime now);

    @Query("SELECT b.borrower.id, COUNT(b) as cnt FROM BorrowRecord b WHERE b.hasException = true GROUP BY b.borrower.id HAVING COUNT(b) >= 3 ORDER BY cnt DESC")
    List<Object[]> findFrequentExceptionUsers();
}
