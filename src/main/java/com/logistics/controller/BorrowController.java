package com.logistics.controller;

import com.logistics.dto.*;
import com.logistics.entity.BorrowRecord;
import com.logistics.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "借用管理", description = "借用申请、审批、归还、异常接口")
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @Operation(summary = "借用申请")
    @PostMapping
    public ApiResponse<BorrowRecord> apply(@RequestBody BorrowRequest request) {
        return ApiResponse.ok(borrowService.apply(request));
    }

    @Operation(summary = "借出确认")
    @PutMapping("/{id}/approve")
    public ApiResponse<BorrowRecord> approve(@PathVariable Long id, @RequestBody BorrowApproveRequest request) {
        return ApiResponse.ok(borrowService.approve(id, request));
    }

    @Operation(summary = "归还登记")
    @PutMapping("/{id}/return")
    public ApiResponse<BorrowRecord> returnItem(@PathVariable Long id, @RequestBody BorrowReturnRequest request) {
        return ApiResponse.ok(borrowService.returnItem(id, request));
    }

    @Operation(summary = "异常备注")
    @PutMapping("/{id}/anomaly")
    public ApiResponse<BorrowRecord> markAnomaly(@PathVariable Long id, @RequestBody BorrowAnomalyRequest request) {
        return ApiResponse.ok(borrowService.markAnomaly(id, request));
    }

    @Operation(summary = "查询借用记录")
    @PostMapping("/query")
    public ApiResponse<List<BorrowRecord>> query(@RequestBody BorrowQueryRequest request) {
        return ApiResponse.ok(borrowService.query(request));
    }

    @Operation(summary = "检查并更新逾期记录")
    @PostMapping("/check-overdue")
    public ApiResponse<Void> checkOverdue() {
        borrowService.checkAndUpdateOverdue();
        return ApiResponse.ok();
    }
}
