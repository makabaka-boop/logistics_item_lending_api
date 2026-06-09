package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.StockAlert;
import com.logistics.itemlending.enums.AlertType;
import com.logistics.itemlending.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@Tag(name = "预警管理", description = "库存预警、逾期提醒等相关接口")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping("/{id}")
    @Operation(summary = "获取预警详情", description = "根据ID获取预警详情")
    public Result<StockAlert> getAlertById(@PathVariable Long id) {
        return Result.success(alertService.getAlertById(id));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将预警标记为已读")
    public Result<StockAlert> markAsRead(@PathVariable Long id) {
        return Result.success(alertService.markAsRead(id));
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将所有未读预警标记为已读")
    public Result<Void> markAllAsRead() {
        alertService.markAllAsRead();
        return Result.successMsg("已全部标记为已读");
    }

    @GetMapping
    @Operation(summary = "分页查询预警列表", description = "分页查询预警记录")
    public Result<Page<StockAlert>> listAlerts(
            @Parameter(description = "预警类型") @RequestParam(required = false) AlertType alertType,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean read,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(alertService.listAlerts(alertType, read, pageable));
    }

    @GetMapping("/unread")
    @Operation(summary = "获取未读预警", description = "获取所有未读预警列表")
    public Result<List<StockAlert>> listUnreadAlerts() {
        return Result.success(alertService.listUnreadAlerts());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读预警数量", description = "获取未读预警的总数")
    public Result<Long> getUnreadCount() {
        return Result.success(alertService.countUnreadAlerts());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预警", description = "删除预警记录")
    public Result<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return Result.successMsg("删除成功");
    }
}
