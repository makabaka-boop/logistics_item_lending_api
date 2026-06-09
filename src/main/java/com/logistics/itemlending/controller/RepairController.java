package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.RepairRecord;
import com.logistics.itemlending.enums.RepairStatus;
import com.logistics.itemlending.service.RepairService;
import com.logistics.itemlending.util.CurrentUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/repair-records")
@Tag(name = "维修管理", description = "物品维修管理相关接口")
public class RepairController {

    @Autowired
    private RepairService repairService;

    @Autowired
    private CurrentUserUtil currentUserUtil;

    @PostMapping("/submit")
    @Operation(summary = "提交维修申请", description = "提交物品维修申请")
    public Result<RepairRecord> submitRepair(@RequestBody Map<String, Object> params) {
        Long itemId = Long.valueOf(params.get("itemId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        String faultDescription = (String) params.get("faultDescription");
        Long reporterId = currentUserUtil.getCurrentUserId();
        return Result.success("提交成功", repairService.submitRepair(itemId, quantity, faultDescription, reporterId));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始维修", description = "开始处理维修")
    public Result<RepairRecord> startRepair(@PathVariable Long id) {
        Long repairerId = currentUserUtil.getCurrentUserId();
        return Result.success("开始维修", repairService.startRepair(id, repairerId));
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "完成维修", description = "完成维修并填写维修说明")
    public Result<RepairRecord> finishRepair(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String repairDescription = params.get("repairDescription");
        return Result.success("维修完成", repairService.finishRepair(id, repairDescription));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "复核维修", description = "复核维修结果")
    public Result<RepairRecord> reviewRepair(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String reviewRemark = (String) params.get("reviewRemark");
        Boolean passed = (Boolean) params.get("passed");
        Long reviewerId = currentUserUtil.getCurrentUserId();
        return Result.success("复核完成", repairService.reviewRepair(id, reviewerId, reviewRemark, passed));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消维修", description = "取消待处理的维修申请")
    public Result<Void> cancelRepair(@PathVariable Long id) {
        repairService.cancelRepair(id);
        return Result.successMsg("取消成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取维修记录详情", description = "根据ID获取维修记录详情")
    public Result<RepairRecord> getRecordById(@PathVariable Long id) {
        return Result.success(repairService.getRecordById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询维修记录", description = "分页查询维修记录，支持多种筛选条件")
    public Result<Page<RepairRecord>> listRecords(
            @Parameter(description = "物品ID") @RequestParam(required = false) Long itemId,
            @Parameter(description = "报修人ID") @RequestParam(required = false) Long reporterId,
            @Parameter(description = "维修人ID") @RequestParam(required = false) Long repairerId,
            @Parameter(description = "维修状态") @RequestParam(required = false) RepairStatus status,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return Result.success(repairService.listRecords(itemId, reporterId, repairerId, status, startTime, endTime, pageable));
    }

    @GetMapping("/pending-review")
    @Operation(summary = "获取待复核维修列表", description = "获取所有待复核的维修记录")
    public Result<List<RepairRecord>> listPendingReviewRecords() {
        return Result.success(repairService.listPendingReviewRecords());
    }

    @GetMapping("/my")
    @Operation(summary = "我的维修记录", description = "获取当前用户提交的维修记录")
    public Result<Page<RepairRecord>> listMyRecords(
            @Parameter(description = "维修状态") @RequestParam(required = false) RepairStatus status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Long myId = currentUserUtil.getCurrentUserId();
        return Result.success(repairService.listRecords(null, myId, null, status, null, null, pageable));
    }
}
