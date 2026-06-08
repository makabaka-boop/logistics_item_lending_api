package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.entity.Item;
import com.logistics.lending.entity.LendingRecord;
import com.logistics.lending.entity.MaintenanceRecord;
import com.logistics.lending.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "统计预警", description = "库存不足、逾期排行、维修待复核、异常人员等统计预警")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘概览", description = "各类预警数量汇总")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.success(statisticsService.getDashboard());
    }

    @GetMapping("/low-stock")
    @Operation(summary = "库存不足清单", description = "可用数量 <= 预警阈值的物品")
    public ApiResponse<List<Item>> lowStock() {
        return ApiResponse.success(statisticsService.getLowStockList());
    }

    @GetMapping("/overdue")
    @Operation(summary = "逾期未归还列表")
    public ApiResponse<List<LendingRecord>> overdue() {
        return ApiResponse.success(statisticsService.getOverdueList());
    }

    @GetMapping("/overdue-ranking")
    @Operation(summary = "逾期归还排行榜", description = "按逾期次数排行")
    public ApiResponse<List<Map<String, Object>>> overdueRanking() {
        return ApiResponse.success(statisticsService.getOverdueRanking());
    }

    @GetMapping("/maintenance-pending-review")
    @Operation(summary = "维修待复核列表")
    public ApiResponse<List<MaintenanceRecord>> maintenancePendingReview() {
        return ApiResponse.success(statisticsService.getMaintenancePendingReview());
    }

    @GetMapping("/unconfirmed-abnormal")
    @Operation(summary = "待确认异常列表")
    public ApiResponse<List<LendingRecord>> unconfirmedAbnormal() {
        return ApiResponse.success(statisticsService.getUnconfirmedAbnormal());
    }

    @GetMapping("/frequent-abnormal-borrowers")
    @Operation(summary = "频繁异常归还人员", description = "异常归还次数 >= 阈值的人员")
    public ApiResponse<List<Map<String, Object>>> frequentAbnormalBorrowers() {
        return ApiResponse.success(statisticsService.getFrequentAbnormalBorrowers());
    }
}
