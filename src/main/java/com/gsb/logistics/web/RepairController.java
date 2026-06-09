package com.gsb.logistics.web;

import com.gsb.logistics.domain.RepairRecord;
import com.gsb.logistics.domain.RepairStatus;
import com.gsb.logistics.service.RepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/repairs")
@Tag(name = "6.维修流转", description = "维修登记/开始/完成/复核")
public class RepairController {
    private final RepairService service;

    public RepairController(RepairService service) { this.service = service; }

    @Data
    public static class RegisterReq {
        private Long itemId;
        private Long lendingId;
        private Integer quantity;
        private String issueDescription;
        private String repairRemark;
        /** 是否从可借库存扣减并转入维修中（异常归还场景下设为 false） */
        private Boolean fromAvailable;
    }

    @Operation(summary = "维修登记")
    @PostMapping
    public ApiResponse<RepairRecord> register(@RequestBody RegisterReq req) {
        RepairRecord r = new RepairRecord();
        r.setItemId(req.getItemId());
        r.setLendingId(req.getLendingId());
        if (req.getQuantity() != null) r.setQuantity(req.getQuantity());
        r.setIssueDescription(req.getIssueDescription());
        r.setRepairRemark(req.getRepairRemark());
        boolean fromAvailable = Boolean.TRUE.equals(req.getFromAvailable());
        return ApiResponse.ok(service.register(r, fromAvailable));
    }

    @Operation(summary = "开始维修")
    @PostMapping("/{id}/start")
    public ApiResponse<RepairRecord> start(@PathVariable Long id) { return ApiResponse.ok(service.start(id)); }

    @Data
    public static class FinishReq { private String repairRemark; }

    @Operation(summary = "完成维修（待复核）")
    @PostMapping("/{id}/finish")
    public ApiResponse<RepairRecord> finish(@PathVariable Long id, @RequestBody(required = false) FinishReq req) {
        return ApiResponse.ok(service.finish(id, req == null ? null : req.getRepairRemark()));
    }

    @Data
    public static class ReviewReq {
        private String reviewer;
        private String reviewRemark;
        /** 是否报废（true: 核减总数；false: 回库可借） */
        private Boolean scrap;
    }

    @Operation(summary = "复核（通过则物品回库或报废）")
    @PostMapping("/{id}/review")
    public ApiResponse<RepairRecord> review(@PathVariable Long id, @RequestBody ReviewReq req) {
        return ApiResponse.ok(service.review(id, req.getReviewer(), req.getReviewRemark(),
                Boolean.TRUE.equals(req.getScrap())));
    }

    @Operation(summary = "查询维修记录")
    @GetMapping
    public ApiResponse<List<RepairRecord>> search(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) RepairStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ApiResponse.ok(service.search(itemId, status, fromDate, toDate));
    }

    @Operation(summary = "维修详情")
    @GetMapping("/{id}")
    public ApiResponse<RepairRecord> get(@PathVariable Long id) { return ApiResponse.ok(service.get(id)); }

    @Operation(summary = "扫描维修完成后未复核且超时的记录，标记为异常")
    @PostMapping("/scan-review-overdue")
    public ApiResponse<Integer> scanReviewOverdue() {
        return ApiResponse.ok(service.scanReviewOverdue());
    }
}
