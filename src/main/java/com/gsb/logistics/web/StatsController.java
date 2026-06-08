package com.gsb.logistics.web;

import com.gsb.logistics.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "7.统计报表", description = "库存预警/逾期排行/维修待复核/总览")
public class StatsController {
    private final StatsService service;

    public StatsController(StatsService service) { this.service = service; }

    @Operation(summary = "库存不足清单")
    @GetMapping("/low-stock")
    public ApiResponse<List<Map<String, Object>>> lowStock() { return ApiResponse.ok(service.lowStock()); }

    @Operation(summary = "逾期归还排行")
    @GetMapping("/overdue-ranking")
    public ApiResponse<List<Map<String, Object>>> overdueRanking() { return ApiResponse.ok(service.overdueRanking()); }

    @Operation(summary = "维修待复核列表")
    @GetMapping("/repair-pending-review")
    public ApiResponse<List<Map<String, Object>>> repairPendingReview() {
        return ApiResponse.ok(service.repairPendingReview());
    }

    @Operation(summary = "维修后未复核超时（异常）清单")
    @GetMapping("/repair-review-overdue")
    public ApiResponse<List<Map<String, Object>>> repairReviewOverdue() {
        return ApiResponse.ok(service.repairReviewOverdue());
    }

    @Operation(summary = "概览数据")
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() { return ApiResponse.ok(service.overview()); }
}
