package com.logistics.itemlending.service;

import com.logistics.itemlending.entity.BorrowRecord;
import com.logistics.itemlending.entity.Item;
import com.logistics.itemlending.entity.StockAlert;
import com.logistics.itemlending.entity.User;
import com.logistics.itemlending.enums.AlertType;
import com.logistics.itemlending.enums.BorrowStatus;
import com.logistics.itemlending.enums.ExceptionType;
import com.logistics.itemlending.exception.BusinessException;
import com.logistics.itemlending.repository.BorrowRecordRepository;
import com.logistics.itemlending.repository.ItemRepository;
import com.logistics.itemlending.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BorrowService {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertService alertService;

    @Value("${borrow.overdue.days:7}")
    private Integer overdueDays;

    @Transactional
    public BorrowRecord applyBorrow(Long itemId, Integer quantity, String reason, LocalDateTime expectedReturnTime, Long borrowerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("物品不存在"));

        if (quantity <= 0) {
            throw new BusinessException("借用数量必须大于0");
        }

        if (item.getAvailableQuantity() < quantity) {
            throw new BusinessException("库存不足，当前可借数量: " + item.getAvailableQuantity());
        }

        if (expectedReturnTime == null) {
            expectedReturnTime = LocalDateTime.now().plusDays(overdueDays);
        }

        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new EntityNotFoundException("借用人不存在"));

        BorrowRecord record = new BorrowRecord();
        record.setRecordNo(generateRecordNo());
        record.setItem(item);
        record.setQuantity(quantity);
        record.setBorrower(borrower);
        record.setBorrowReason(reason);
        record.setExpectedReturnTime(expectedReturnTime);
        record.setStatus(BorrowStatus.PENDING);

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord confirmBorrow(Long recordId, Long confirmerId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("借用记录不存在"));

        if (!BorrowStatus.PENDING.equals(record.getStatus())) {
            throw new BusinessException("只有待确认的借用申请才能确认借出");
        }

        Item item = record.getItem();
        if (item.getAvailableQuantity() < record.getQuantity()) {
            throw new BusinessException("库存不足，当前可借数量: " + item.getAvailableQuantity());
        }

        User confirmer = userRepository.findById(confirmerId)
                .orElseThrow(() -> new EntityNotFoundException("确认人不存在"));

        item.setAvailableQuantity(item.getAvailableQuantity() - record.getQuantity());
        item.setBorrowedQuantity(item.getBorrowedQuantity() + record.getQuantity());
        itemRepository.save(item);

        record.setStatus(BorrowStatus.BORROWED);
        record.setBorrowTime(LocalDateTime.now());
        record.setBorrowConfirmer(confirmer);

        checkLowStockAlert(item);

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord returnItem(Long recordId, String returnRemark, Long confirmerId) {
        return returnItemWithException(recordId, returnRemark, confirmerId, false, null, null);
    }

    @Transactional
    public BorrowRecord returnItemWithException(Long recordId, String returnRemark, Long confirmerId,
                                                Boolean hasException, ExceptionType exceptionType, String exceptionDescription) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("借用记录不存在"));

        if (!BorrowStatus.BORROWED.equals(record.getStatus()) && !BorrowStatus.OVERDUE.equals(record.getStatus())) {
            throw new BusinessException("只有借出状态的物品才能归还");
        }

        Item item = record.getItem();
        item.setAvailableQuantity(item.getAvailableQuantity() + record.getQuantity());
        item.setBorrowedQuantity(item.getBorrowedQuantity() - record.getQuantity());
        if (item.getBorrowedQuantity() < 0) {
            item.setBorrowedQuantity(0);
        }
        itemRepository.save(item);

        User confirmer = userRepository.findById(confirmerId)
                .orElseThrow(() -> new EntityNotFoundException("确认人不存在"));

        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnTime(LocalDateTime.now());
        record.setReturnConfirmer(confirmer);
        record.setReturnRemark(returnRemark);

        if (Boolean.TRUE.equals(hasException)) {
            record.setHasException(true);
            record.setExceptionType(exceptionType);
            record.setExceptionDescription(exceptionDescription);
            record.setStatus(BorrowStatus.EXCEPTION);
            createExceptionAlert(record);
        }

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord confirmException(Long recordId, String confirmRemark) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("借用记录不存在"));

        if (!BorrowStatus.EXCEPTION.equals(record.getStatus())) {
            throw new BusinessException("只有异常状态的记录才能确认");
        }

        String currentRemark = record.getExceptionDescription() != null ? record.getExceptionDescription() : "";
        record.setExceptionDescription(currentRemark + "\n[异常确认] " + confirmRemark);

        return borrowRecordRepository.save(record);
    }

    public BorrowRecord getRecordById(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("借用记录不存在"));
    }

    public Page<BorrowRecord> listRecords(Long itemId, Long borrowerId, BorrowStatus status,
                                          ExceptionType exceptionType, LocalDateTime startTime,
                                          LocalDateTime endTime, Pageable pageable) {
        Specification<BorrowRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (itemId != null) {
                predicates.add(cb.equal(root.get("item").get("id"), itemId));
            }

            if (borrowerId != null) {
                predicates.add(cb.equal(root.get("borrower").get("id"), borrowerId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (exceptionType != null) {
                predicates.add(cb.equal(root.get("exceptionType"), exceptionType));
            }

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return borrowRecordRepository.findAll(spec, pageable);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkOverdueRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(BorrowStatus.BORROWED, now);

        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowStatus.OVERDUE);
            borrowRecordRepository.save(record);
            createOverdueAlert(record);
        }
    }

    public void cancelBorrow(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("借用记录不存在"));

        if (!BorrowStatus.PENDING.equals(record.getStatus())) {
            throw new BusinessException("只有待确认的借用申请才能取消");
        }

        borrowRecordRepository.delete(record);
    }

    private void checkLowStockAlert(Item item) {
        if (item.getAvailableQuantity() <= item.getWarningThreshold()) {
            if (!alertService.existsStockAlert(AlertType.STOCK_LOW, item.getId())) {
                StockAlert alert = new StockAlert();
                alert.setAlertType(AlertType.STOCK_LOW);
                alert.setTitle("库存不足预警");
                alert.setContent("物品 " + item.getName() + " 库存不足，当前可用数量: " + item.getAvailableQuantity());
                alert.setItem(item);
                alertService.createAlert(alert);
            }
        }
    }

    private void createOverdueAlert(BorrowRecord record) {
        StockAlert alert = new StockAlert();
        alert.setAlertType(AlertType.OVERDUE);
        alert.setTitle("物品逾期未归还");
        alert.setContent("物品 " + record.getItem().getName() + " 已逾期，借用人: " + record.getBorrower().getRealName());
        alert.setItem(record.getItem());
        alert.setRelatedUser(record.getBorrower());
        alert.setRelatedBorrowRecord(record);
        alertService.createAlert(alert);
    }

    private void createExceptionAlert(BorrowRecord record) {
        StockAlert alert = new StockAlert();
        alert.setAlertType(AlertType.FREQUENT_EXCEPTION);
        alert.setTitle("物品归还异常");
        alert.setContent("物品 " + record.getItem().getName() + " 归还异常，类型: " + record.getExceptionType());
        alert.setItem(record.getItem());
        alert.setRelatedUser(record.getBorrower());
        alert.setRelatedBorrowRecord(record);
        alertService.createAlert(alert);
    }

    private String generateRecordNo() {
        return "BR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
