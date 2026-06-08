package com.gsb.logistics.service;

import com.gsb.logistics.config.AppProperties;
import com.gsb.logistics.domain.*;
import com.gsb.logistics.repository.LendingRecordRepository;
import com.gsb.logistics.repository.SupplementRequestRepository;
import com.gsb.logistics.web.BusinessException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LendingService {
    private final LendingRecordRepository repo;
    private final SupplementRequestRepository supplementRepo;
    private final ItemService itemService;
    private final AppProperties props;

    public LendingService(LendingRecordRepository repo,
                          SupplementRequestRepository supplementRepo,
                          ItemService itemService, AppProperties props) {
        this.repo = repo;
        this.supplementRepo = supplementRepo;
        this.itemService = itemService;
        this.props = props;
    }

    /** 提交借用申请 */
    public LendingRecord apply(LendingRecord r) {
        if (r.getItemId() == null) throw new BusinessException("itemId 不能为空");
        if (r.getBorrower() == null || r.getBorrower().isBlank()) throw new BusinessException("borrower 不能为空");
        if (r.getQuantity() == null || r.getQuantity() <= 0) r.setQuantity(1);
        Item it = itemService.get(r.getItemId());
        if (it.getAvailableQuantity() < r.getQuantity()) {
            throw new BusinessException(409, "库存不足，可借数量: " + it.getAvailableQuantity());
        }
        r.setId(null);
        r.setStatus(LendingStatus.APPLIED);
        r.setApplyTime(LocalDateTime.now());
        r.setAbnormalType(AbnormalType.NONE);
        return repo.save(r);
    }

    /** 借出确认 */
    @Transactional
    public LendingRecord confirmLend(Long id, LocalDateTime expectReturnTime) {
        LendingRecord r = repo.findById(id).orElseThrow(() -> new BusinessException(404, "借用记录不存在"));
        if (r.getStatus() != LendingStatus.APPLIED) {
            throw new BusinessException("当前状态不可借出，状态: " + r.getStatus());
        }
        // 拦截库存不足
        itemService.decreaseAvailable(r.getItemId(), r.getQuantity());
        r.setStatus(LendingStatus.LENT);
        r.setLendTime(LocalDateTime.now());
        if (expectReturnTime != null) r.setExpectReturnTime(expectReturnTime);
        else r.setExpectReturnTime(LocalDateTime.now().plusDays(props.getRules().getOverdueDays()));
        return repo.save(r);
    }

    /** 驳回 */
    public LendingRecord reject(Long id, String reason) {
        LendingRecord r = repo.findById(id).orElseThrow(() -> new BusinessException(404, "借用记录不存在"));
        if (r.getStatus() != LendingStatus.APPLIED) throw new BusinessException("仅 APPLIED 状态可驳回");
        r.setStatus(LendingStatus.REJECTED);
        r.setAbnormalRemark(reason);
        return repo.save(r);
    }

    /** 归还登记 */
    @Transactional
    public LendingRecord returnItem(Long id, String returnRemark, AbnormalType abnormalType, String abnormalRemark) {
        LendingRecord r = repo.findById(id).orElseThrow(() -> new BusinessException(404, "借用记录不存在"));
        if (r.getStatus() != LendingStatus.LENT && r.getStatus() != LendingStatus.OVERDUE) {
            throw new BusinessException("当前状态不可归还，状态: " + r.getStatus());
        }
        LocalDateTime now = LocalDateTime.now();
        r.setReturnTime(now);
        r.setReturnRemark(returnRemark);

        // 自动判定逾期
        boolean overdue = r.getExpectReturnTime() != null && now.isAfter(r.getExpectReturnTime());
        if (abnormalType == null || abnormalType == AbnormalType.NONE) {
            if (overdue) {
                r.setAbnormalType(AbnormalType.OVERDUE);
                r.setStatus(LendingStatus.ABNORMAL);
            } else {
                r.setAbnormalType(AbnormalType.NONE);
                r.setStatus(LendingStatus.RETURNED);
            }
        } else {
            r.setAbnormalType(abnormalType);
            r.setStatus(LendingStatus.ABNORMAL);
        }
        if (abnormalRemark != null) r.setAbnormalRemark(abnormalRemark);

        // 归还入库（损坏/丢失数量不入库，由后续维修流程处理）
        boolean restore = abnormalType == null || abnormalType == AbnormalType.NONE
                || abnormalType == AbnormalType.OVERDUE
                || abnormalType == AbnormalType.OTHER;
        if (restore) {
            itemService.increaseAvailable(r.getItemId(), r.getQuantity());
        } else if (abnormalType == AbnormalType.DAMAGED) {
            // 损坏的物品转入维修中
            itemService.moveToRepair(r.getItemId(), r.getQuantity());
        } else if (abnormalType == AbnormalType.LOST) {
            // 丢失：直接核减总数量（持久化）
            itemService.decreaseTotal(r.getItemId(), r.getQuantity());
        }

        LendingRecord saved = repo.save(r);

        // 检测同一责任人连续异常归还
        checkRepeatedAbnormal(saved);
        return saved;
    }

    /** 异常确认 */
    public LendingRecord confirmAbnormal(Long id, AbnormalType type, String remark, String confirmer) {
        LendingRecord r = repo.findById(id).orElseThrow(() -> new BusinessException(404, "借用记录不存在"));
        r.setAbnormalType(type == null ? AbnormalType.OTHER : type);
        if (remark != null) r.setAbnormalRemark(remark);
        r.setAbnormalConfirmedBy(confirmer);
        r.setAbnormalConfirmedAt(LocalDateTime.now());
        if (r.getStatus() == LendingStatus.LENT || r.getStatus() == LendingStatus.OVERDUE) {
            r.setStatus(LendingStatus.ABNORMAL);
        }
        return repo.save(r);
    }

    private void checkRepeatedAbnormal(LendingRecord saved) {
        int streak = props.getRules().getAbnormalStreak();
        List<LendingRecord> recent = repo.findTop20ByBorrowerOrderByApplyTimeDesc(saved.getBorrower());
        // 取已结束（非 APPLIED/LENT）的最近若干次
        List<LendingRecord> finished = new ArrayList<>();
        for (LendingRecord lr : recent) {
            if (lr.getStatus() == LendingStatus.RETURNED
                    || lr.getStatus() == LendingStatus.ABNORMAL
                    || lr.getStatus() == LendingStatus.OVERDUE) {
                finished.add(lr);
            }
            if (finished.size() >= streak) break;
        }
        if (finished.size() < streak) return;
        boolean allAbnormal = finished.stream().allMatch(
                l -> l.getStatus() == LendingStatus.ABNORMAL || l.getStatus() == LendingStatus.OVERDUE);
        if (allAbnormal) {
            saved.setAbnormalType(AbnormalType.REPEATED_ABNORMAL);
            saved.setAbnormalRemark((saved.getAbnormalRemark() == null ? "" : saved.getAbnormalRemark() + " | ")
                    + "检测到连续 " + streak + " 次异常归还");
            repo.save(saved);
        }
    }

    /** 列表查询 */
    public List<LendingRecord> search(Long itemId, Long categoryId, Long locationId, String borrower,
                                      LendingStatus status, AbnormalType abnormalType,
                                      LocalDateTime fromDate, LocalDateTime toDate) {
        // 先按 lending 自身条件查
        Specification<LendingRecord> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (itemId != null) ps.add(cb.equal(root.get("itemId"), itemId));
            if (borrower != null && !borrower.isBlank()) ps.add(cb.equal(root.get("borrower"), borrower));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (abnormalType != null) ps.add(cb.equal(root.get("abnormalType"), abnormalType));
            if (fromDate != null) ps.add(cb.greaterThanOrEqualTo(root.get("applyTime"), fromDate));
            if (toDate != null) ps.add(cb.lessThanOrEqualTo(root.get("applyTime"), toDate));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        List<LendingRecord> list = repo.findAll(spec);
        // 按物品分类/位置筛选（关联过滤）
        if (categoryId != null || locationId != null) {
            list.removeIf(lr -> {
                Item it = itemService.get(lr.getItemId());
                if (categoryId != null && !categoryId.equals(it.getCategoryId())) return true;
                if (locationId != null && !locationId.equals(it.getLocationId())) return true;
                return false;
            });
        }
        list.sort(Comparator.comparing(LendingRecord::getApplyTime).reversed());
        return list;
    }

    /** 标记逾期（扫描） */
    @Transactional
    public int scanOverdue() {
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (LendingRecord r : repo.findByStatus(LendingStatus.LENT)) {
            if (r.getExpectReturnTime() != null && now.isAfter(r.getExpectReturnTime())) {
                r.setStatus(LendingStatus.OVERDUE);
                r.setAbnormalType(AbnormalType.OVERDUE);
                repo.save(r);
                count++;
            }
        }
        return count;
    }

    /** 提交补充申请 */
    public SupplementRequest submitSupplement(Long lendingId, int extraQuantity, String reason, String submittedBy) {
        LendingRecord r = repo.findById(lendingId).orElseThrow(() -> new BusinessException(404, "借用记录不存在"));
        if (r.getStatus() != LendingStatus.LENT && r.getStatus() != LendingStatus.OVERDUE) {
            throw new BusinessException("仅借出中的记录可提交补充申请");
        }
        if (extraQuantity <= 0) throw new BusinessException("extraQuantity 必须 > 0");
        if (submittedBy == null || submittedBy.isBlank()) {
            // 默认使用借用人作为提交人
            submittedBy = r.getBorrower();
        }
        SupplementRequest s = new SupplementRequest();
        s.setLendingId(lendingId);
        s.setExtraQuantity(extraQuantity);
        s.setReason(reason);
        s.setSubmittedBy(submittedBy);
        return supplementRepo.save(s);
    }

    /** 处理补充申请 */
    @Transactional
    public SupplementRequest processSupplement(Long supplementId, boolean approve, String processor, String remark) {
        SupplementRequest s = supplementRepo.findById(supplementId)
                .orElseThrow(() -> new BusinessException(404, "补充申请不存在"));
        if (!"PENDING".equals(s.getStatus())) throw new BusinessException("已处理，当前状态: " + s.getStatus());
        if (approve) {
            LendingRecord r = repo.findById(s.getLendingId())
                    .orElseThrow(() -> new BusinessException(404, "关联的借用记录不存在"));
            itemService.decreaseAvailable(r.getItemId(), s.getExtraQuantity());
            r.setQuantity(r.getQuantity() + s.getExtraQuantity());
            repo.save(r);
            s.setStatus("APPROVED");
        } else {
            s.setStatus("REJECTED");
        }
        s.setProcessedAt(LocalDateTime.now());
        s.setProcessedBy(processor);
        s.setProcessRemark(remark);
        return supplementRepo.save(s);
    }

    public List<SupplementRequest> listSupplements(Long lendingId) {
        if (lendingId != null) return supplementRepo.findByLendingId(lendingId);
        return supplementRepo.findAll();
    }
}
