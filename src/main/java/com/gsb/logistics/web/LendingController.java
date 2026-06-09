package com.gsb.logistics.web;

import com.gsb.logistics.domain.AbnormalType;
import com.gsb.logistics.domain.LendingRecord;
import com.gsb.logistics.domain.LendingStatus;
import com.gsb.logistics.domain.SupplementRequest;
import com.gsb.logistics.service.LendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lendings")
@Tag(name = "5.借用流转", description = "借用申请/借出确认/归还/异常确认/补充申请/逾期扫描")
public class LendingController {
    private final LendingService service;

    public LendingController(LendingService service) { this.service = service; }

    @Operation(summary = "提交借用申请")
    @PostMapping("/apply")
    public ApiResponse<LendingRecord> apply(@RequestBody LendingRecord r) {
        return ApiResponse.ok(service.apply(r));
    }

    @Data
    public static class ConfirmLendReq {
        @Parameter(description = "期望归还时间，格式 yyyy-MM-ddTHH:mm:ss")
        private LocalDateTime expectReturnTime;
    }

    @Operation(summary = "借出确认（库存不足时拒绝）")
    @PostMapping("/{id}/confirm-lend")
    public ApiResponse<LendingRecord> confirmLend(@PathVariable Long id, @RequestBody(required = false) ConfirmLendReq req) {
        return ApiResponse.ok(service.confirmLend(id, req == null ? null : req.getExpectReturnTime()));
    }

    @Data
    public static class RejectReq { private String reason; }

    @Operation(summary = "驳回借用申请")
    @PostMapping("/{id}/reject")
    public ApiResponse<LendingRecord> reject(@PathVariable Long id, @RequestBody(required = false) RejectReq req) {
        return ApiResponse.ok(service.reject(id, req == null ? null : req.getReason()));
    }

    @Data
    public static class ReturnReq {
        private String returnRemark;
        private AbnormalType abnormalType;
        private String abnormalRemark;
    }

    @Operation(summary = "归还登记（自动判定逾期，损坏自动转维修，丢失自动核减）")
    @PostMapping("/{id}/return")
    public ApiResponse<LendingRecord> returnItem(@PathVariable Long id, @RequestBody(required = false) ReturnReq req) {
        if (req == null) req = new ReturnReq();
        return ApiResponse.ok(service.returnItem(id, req.getReturnRemark(), req.getAbnormalType(), req.getAbnormalRemark()));
    }

    @Data
    public static class AbnormalConfirmReq {
        private AbnormalType abnormalType;
        private String remark;
        private String confirmer;
    }

    @Operation(summary = "异常确认")
    @PostMapping("/{id}/abnormal")
    public ApiResponse<LendingRecord> confirmAbnormal(@PathVariable Long id, @RequestBody AbnormalConfirmReq req) {
        return ApiResponse.ok(service.confirmAbnormal(id, req.getAbnormalType(), req.getRemark(), req.getConfirmer()));
    }

    @Operation(summary = "借用记录查询",
            description = "支持按物品、分类、位置、借用人、状态、异常类型、日期范围筛选")
    @GetMapping
    public ApiResponse<List<LendingRecord>> search(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String borrower,
            @RequestParam(required = false) LendingStatus status,
            @RequestParam(required = false) AbnormalType abnormalType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ApiResponse.ok(service.search(itemId, categoryId, locationId, borrower,
                status, abnormalType, fromDate, toDate));
    }

    @Operation(summary = "扫描逾期记录（手动触发）")
    @PostMapping("/scan-overdue")
    public ApiResponse<Integer> scanOverdue() { return ApiResponse.ok(service.scanOverdue()); }

    @Data
    public static class SupplementReq {
        private Long lendingId;
        private Integer extraQuantity;
        private String reason;
        @Parameter(description = "提交人；为空时默认取借用人")
        private String submittedBy;
    }

    @Operation(summary = "提交补充申请（针对已借出的记录追加数量）")
    @PostMapping("/supplements")
    public ApiResponse<SupplementRequest> submitSupplement(@RequestBody SupplementReq req) {
        return ApiResponse.ok(service.submitSupplement(req.getLendingId(),
                req.getExtraQuantity() == null ? 0 : req.getExtraQuantity(),
                req.getReason(), req.getSubmittedBy()));
    }

    @Data
    public static class ProcessSupplementReq {
        private Boolean approve;
        private String processor;
        private String remark;
    }

    @Operation(summary = "处理补充申请")
    @PostMapping("/supplements/{id}/process")
    public ApiResponse<SupplementRequest> processSupplement(@PathVariable Long id,
                                                            @RequestBody ProcessSupplementReq req) {
        return ApiResponse.ok(service.processSupplement(id,
                Boolean.TRUE.equals(req.getApprove()), req.getProcessor(), req.getRemark()));
    }

    @Operation(summary = "查询补充申请列表")
    @GetMapping("/supplements")
    public ApiResponse<List<SupplementRequest>> listSupplements(@RequestParam(required = false) Long lendingId) {
        return ApiResponse.ok(service.listSupplements(lendingId));
    }
}
