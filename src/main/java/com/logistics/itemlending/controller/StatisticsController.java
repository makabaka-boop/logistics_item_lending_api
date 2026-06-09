package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.Item;
import com.logistics.itemlending.entity.RepairRecord;
import com.logistics.itemlending.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@Tag(name = "统计管理", description = "库存、逾期、维修等统计相关接口")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘统计", description = "获取仪表盘汇总统计数据")
    public Result<Map<String, Object>> getDashboardStatistics() {
        return Result.success(statisticsService.getDashboardStatistics());
    }

    @GetMapping("/low-stock")
    @Operation(summary = "库存不足清单", description = "获取库存不足的物品清单")
    public Result<List<Item>> getLowStockList() {
        return Result.success(statisticsService.getLowStockList());
    }

    @GetMapping("/overdue-ranking")
    @Operation(summary = "逾期归还排行", description = "获取逾期归还次数排行榜")
    public Result<List<Map<String, Object>>> getOverdueRanking() {
        return Result.success(statisticsService.getOverdueRanking());
    }

    @GetMapping("/repair-pending-review")
    @Operation(summary = "维修待复核列表", description = "获取维修待复核的记录列表")
    public Result<List<RepairRecord>> getRepairPendingReviewList() {
        return Result.success(statisticsService.getRepairPendingReviewList());
    }

    @GetMapping("/frequent-exception-users")
    @Operation(summary = "频繁异常用户", description = "获取连续异常归还的用户列表")
    public Result<List<Map<String, Object>>> getFrequentExceptionUsers() {
        return Result.success(statisticsService.getFrequentExceptionUsers());
    }
}
