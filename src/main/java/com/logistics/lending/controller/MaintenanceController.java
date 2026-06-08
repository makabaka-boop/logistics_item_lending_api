package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.dto.MaintenanceCompleteRequest;
import com.logistics.lending.dto.MaintenanceRequest;
import com.logistics.lending.dto.MaintenanceReviewRequest;
import com.logistics.lending.entity.MaintenanceRecord;
import com.logistics.lending.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Tag(name = "维修管理", description = "维修登记、维修完成、维修复核")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    @Operation(summary = "提交维修登记", description = "将物品从可用库转入维修中")
    public ApiResponse<MaintenanceRecord> submit(@Valid @RequestBody MaintenanceRequest request) {
        return ApiResponse.success(maintenanceService.submit(request));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "维修完成", description = "维修完成后状态变为待复核")
    public ApiResponse<MaintenanceRecord> complete(@PathVariable Long id, @RequestBody MaintenanceCompleteRequest request) {
        return ApiResponse.success(maintenanceService.complete(id, request));
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "维修复核", description = "复核通过后物品返回可用库存")
    public ApiResponse<MaintenanceRecord> review(@PathVariable Long id, @RequestBody MaintenanceReviewRequest request) {
        return ApiResponse.success(maintenanceService.review(id, request));
    }

    @GetMapping
    @Operation(summary = "获取维修记录列表", description = "可按状态筛选")
    public ApiResponse<List<MaintenanceRecord>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(maintenanceService.list(status));
    }

    @GetMapping("/pending-review")
    @Operation(summary = "获取待复核维修列表")
    public ApiResponse<List<MaintenanceRecord>> pendingReview() {
        return ApiResponse.success(maintenanceService.listPendingReview());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取维修记录详情")
    public ApiResponse<MaintenanceRecord> getById(@PathVariable Long id) {
        return ApiResponse.success(maintenanceService.getById(id));
    }
}
