package com.logistics.lending.repository;

import com.logistics.lending.entity.LendingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LendingRecordRepository extends JpaRepository<LendingRecord, Long> {

    List<LendingRecord> findByStatus(String status);

    List<LendingRecord> findByItemId(Long itemId);

    List<LendingRecord> findByBorrowerNameContaining(String borrowerName);

    @Query("SELECT l FROM LendingRecord l WHERE l.status = 'LENT' AND l.expectedReturnDate < :now")
    List<LendingRecord> findOverdueRecords(@Param("now") LocalDateTime now);

    @Query("SELECT l FROM LendingRecord l WHERE l.expectedReturnDate IS NOT NULL AND " +
           "( (l.status = 'LENT' AND l.expectedReturnDate < :now) OR " +
           "  (l.status IN ('RETURNED','ABNORMAL','ABNORMAL_CONFIRMED') AND l.returnedAt > l.expectedReturnDate) )")
    List<LendingRecord> findAllOverdueRecords(@Param("now") LocalDateTime now);

    @Query("SELECT l.borrowerName, COUNT(l) as cnt FROM LendingRecord l WHERE l.expectedReturnDate IS NOT NULL AND " +
           "( (l.status = 'LENT' AND l.expectedReturnDate < :now) OR " +
           "  (l.status IN ('RETURNED','ABNORMAL','ABNORMAL_CONFIRMED') AND l.returnedAt > l.expectedReturnDate) ) " +
           "GROUP BY l.borrowerName ORDER BY cnt DESC")
    List<Object[]> findOverdueBorrowersRanking(@Param("now") LocalDateTime now);

    @Query("SELECT l FROM LendingRecord l WHERE l.abnormalType IS NOT NULL AND l.abnormalConfirmed = false")
    List<LendingRecord> findUnconfirmedAbnormalRecords();

    @Query("SELECT COUNT(l) FROM LendingRecord l WHERE l.borrowerName = :borrowerName AND l.abnormalType IS NOT NULL")
    long countAbnormalByBorrower(@Param("borrowerName") String borrowerName);

    @Query("SELECT l FROM LendingRecord l WHERE " +
           "(:categoryId IS NULL OR l.item.category.id = :categoryId) AND " +
           "(:locationId IS NULL OR l.item.location.id = :locationId) AND " +
           "(:borrowerName IS NULL OR l.borrowerName LIKE %:borrowerName%) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:abnormalType IS NULL OR l.abnormalType = :abnormalType) AND " +
           "(:startDate IS NULL OR l.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR l.createdAt <= :endDate)")
    List<LendingRecord> searchRecords(
            @Param("categoryId") Long categoryId,
            @Param("locationId") Long locationId,
            @Param("borrowerName") String borrowerName,
            @Param("status") String status,
            @Param("abnormalType") String abnormalType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
