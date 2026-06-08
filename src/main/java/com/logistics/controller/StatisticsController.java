package com.logistics.controller;

import com.logistics.dto.ApiResponse;
import com.logistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "统计接口", description = "库存不足清单、逾期排行、维修待复核、连续异常")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "库存不足清单")
    @GetMapping("/low-stock")
    public ApiResponse<Map<String, Object>> getLowStockList(@RequestParam(required = false) Integer threshold) {
        return ApiResponse.ok(statisticsService.getLowStockList(threshold));
    }

    @Operation(summary = "逾期归还排行")
    @GetMapping("/overdue-ranking")
    public ApiResponse<Map<String, Object>> getOverdueRanking() {
        return ApiResponse.ok(statisticsService.getOverdueRanking());
    }

    @Operation(summary = "维修待复核列表")
    @GetMapping("/repair-pending-review")
    public ApiResponse<Map<String, Object>> getRepairPendingReview() {
        return ApiResponse.ok(statisticsService.getRepairPendingReview());
    }

    @Operation(summary = "连续异常归还检测")
    @GetMapping("/consecutive-anomaly")
    public ApiResponse<Map<String, Object>> getConsecutiveAnomaly(
            @RequestParam String responsiblePerson,
            @RequestParam(required = false) Integer threshold) {
        return ApiResponse.ok(statisticsService.getConsecutiveAnomaly(responsiblePerson, threshold));
    }
}
