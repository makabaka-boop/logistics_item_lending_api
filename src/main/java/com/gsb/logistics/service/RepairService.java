package com.gsb.logistics.service;

import com.gsb.logistics.config.AppProperties;
import com.gsb.logistics.domain.RepairRecord;
import com.gsb.logistics.domain.RepairStatus;
import com.gsb.logistics.repository.RepairRecordRepository;
import com.gsb.logistics.web.BusinessException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepairService {
    private final RepairRecordRepository repo;
    private final ItemService itemService;
    private final AppProperties props;

    public RepairService(RepairRecordRepository repo, ItemService itemService, AppProperties props) {
        this.repo = repo;
        this.itemService = itemService;
        this.props = props;
    }

    /** 维修登记 */
    @Transactional
    public RepairRecord register(RepairRecord r, boolean fromAvailable) {
        if (r.getItemId() == null) throw new BusinessException("itemId 不能为空");
        if (r.getQuantity() == null || r.getQuantity() <= 0) r.setQuantity(1);
        if (r.getIssueDescription() == null || r.getIssueDescription().isBlank())
            throw new BusinessException("issueDescription 不能为空");
        r.setId(null);
        r.setStatus(RepairStatus.REGISTERED);
        r.setRegisterTime(LocalDateTime.now());
        // 如果是从可借库存中转出维修，需扣减 available 并增加 repairing
        if (fromAvailable) {
            itemService.decreaseAvailable(r.getItemId(), r.getQuantity());
            itemService.moveToRepair(r.getItemId(), r.getQuantity());
        }
        return repo.save(r);
    }

    public RepairRecord start(Long id) {
        RepairRecord r = get(id);
        if (r.getStatus() != RepairStatus.REGISTERED) throw new BusinessException("当前状态不可开始维修");
        r.setStatus(RepairStatus.IN_PROGRESS);
        r.setStartTime(LocalDateTime.now());
        return repo.save(r);
    }

    public RepairRecord finish(Long id, String repairRemark) {
        RepairRecord r = get(id);
        if (r.getStatus() != RepairStatus.IN_PROGRESS && r.getStatus() != RepairStatus.REGISTERED) {
            throw new BusinessException("当前状态不可完成维修");
        }
        r.setStatus(RepairStatus.DONE_PENDING_REVIEW);
        r.setFinishTime(LocalDateTime.now());
        if (repairRemark != null) r.setRepairRemark(repairRemark);
        return repo.save(r);
    }

    @Transactional
    public RepairRecord review(Long id, String reviewer, String reviewRemark, boolean scrap) {
        RepairRecord r = get(id);
        if (r.getStatus() != RepairStatus.DONE_PENDING_REVIEW) {
            throw new BusinessException("仅 DONE_PENDING_REVIEW 状态可复核");
        }
        r.setStatus(scrap ? RepairStatus.SCRAPPED : RepairStatus.REVIEWED);
        r.setReviewTime(LocalDateTime.now());
        r.setReviewer(reviewer);
        r.setReviewRemark(reviewRemark);
        r.setReviewOverdue(false);
        itemService.repairBack(r.getItemId(), r.getQuantity(), scrap);
        return repo.save(r);
    }

    /** 扫描维修完成后未复核且超时的记录，标记 reviewOverdue */
    @Transactional
    public int scanReviewOverdue() {
        int hours = props.getRules().getRepairReviewHours();
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        int count = 0;
        for (RepairRecord r : repo.findByStatus(RepairStatus.DONE_PENDING_REVIEW)) {
            if (r.getFinishTime() != null && r.getFinishTime().isBefore(threshold)
                    && !Boolean.TRUE.equals(r.getReviewOverdue())) {
                r.setReviewOverdue(true);
                repo.save(r);
                count++;
            }
        }
        return count;
    }

    public RepairRecord get(Long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException(404, "维修记录不存在"));
    }

    public List<RepairRecord> search(Long itemId, RepairStatus status,
                                     LocalDateTime fromDate, LocalDateTime toDate) {
        Specification<RepairRecord> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (itemId != null) ps.add(cb.equal(root.get("itemId"), itemId));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (fromDate != null) ps.add(cb.greaterThanOrEqualTo(root.get("registerTime"), fromDate));
            if (toDate != null) ps.add(cb.lessThanOrEqualTo(root.get("registerTime"), toDate));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return repo.findAll(spec);
    }

    public List<RepairRecord> pendingReview() {
        return repo.findByStatus(RepairStatus.DONE_PENDING_REVIEW);
    }
}
