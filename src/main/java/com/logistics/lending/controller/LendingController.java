package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.dto.LendingRequest;
import com.logistics.lending.dto.ReturnRequest;
import com.logistics.lending.entity.LendingRecord;
import com.logistics.lending.service.LendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lending")
@RequiredArgsConstructor
@Tag(name = "借还管理", description = "借用申请、借出确认、归还登记、异常确认")
public class LendingController {

    private final LendingService lendingService;

    @PostMapping("/apply")
    @Operation(summary = "提交借用申请")
    public ApiResponse<LendingRecord> apply(@Valid @RequestBody LendingRequest request) {
        return ApiResponse.success(lendingService.applyLending(request));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "借出确认", description = "确认后扣减库存，状态变为LENT")
    public ApiResponse<LendingRecord> confirm(@PathVariable Long id) {
        return ApiResponse.success(lendingService.confirmLending(id));
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "归还登记", description = "归还时可填写异常类型和备注")
    public ApiResponse<LendingRecord> returnItem(@PathVariable Long id, @RequestBody ReturnRequest request) {
        return ApiResponse.success(lendingService.returnItem(id, request));
    }

    @PutMapping("/{id}/confirm-abnormal")
    @Operation(summary = "异常确认", description = "对异常归还进行确认")
    public ApiResponse<LendingRecord> confirmAbnormal(@PathVariable Long id) {
        return ApiResponse.success(lendingService.confirmAbnormal(id));
    }

    @GetMapping("/search")
    @Operation(summary = "查询借还记录", description = "支持按分类、位置、借用人、状态、异常类型、日期范围筛选")
    public ApiResponse<List<LendingRecord>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String borrowerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String abnormalType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.success(lendingService.search(categoryId, locationId, borrowerName, status, abnormalType, startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取借还记录详情")
    public ApiResponse<LendingRecord> getById(@PathVariable Long id) {
        return ApiResponse.success(lendingService.getById(id));
    }
}
