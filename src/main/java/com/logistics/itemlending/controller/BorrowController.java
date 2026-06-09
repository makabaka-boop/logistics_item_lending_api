package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.BorrowRecord;
import com.logistics.itemlending.enums.BorrowStatus;
import com.logistics.itemlending.enums.ExceptionType;
import com.logistics.itemlending.service.BorrowService;
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
import java.util.Map;

@RestController
@RequestMapping("/borrow-records")
@Tag(name = "借用管理", description = "物品借用归还相关接口")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private CurrentUserUtil currentUserUtil;

    @PostMapping("/apply")
    @Operation(summary = "提交借用申请", description = "提交物品借用申请")
    public Result<BorrowRecord> applyBorrow(@RequestBody Map<String, Object> params) {
        Long itemId = Long.valueOf(params.get("itemId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        String reason = (String) params.get("reason");
        LocalDateTime expectedReturnTime = null;
        if (params.get("expectedReturnTime") != null) {
            expectedReturnTime = LocalDateTime.parse(params.get("expectedReturnTime").toString());
        }
        Long borrowerId = currentUserUtil.getCurrentUserId();
        return Result.success("申请提交成功", borrowService.applyBorrow(itemId, quantity, reason, expectedReturnTime, borrowerId));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认借出", description = "确认物品借出，管理员操作")
    public Result<BorrowRecord> confirmBorrow(@PathVariable Long id) {
        Long confirmerId = currentUserUtil.getCurrentUserId();
        return Result.success("借出确认成功", borrowService.confirmBorrow(id, confirmerId));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "归还物品", description = "归还物品")
    public Result<BorrowRecord> returnItem(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        String returnRemark = null;
        if (params != null) {
            returnRemark = (String) params.get("returnRemark");
        }
        Long confirmerId = currentUserUtil.getCurrentUserId();
        return Result.success("归还成功", borrowService.returnItem(id, returnRemark, confirmerId));
    }

    @PostMapping("/{id}/return-with-exception")
    @Operation(summary = "异常归还", description = "归还物品并标注异常")
    public Result<BorrowRecord> returnItemWithException(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String returnRemark = (String) params.get("returnRemark");
        Boolean hasException = (Boolean) params.get("hasException");
        ExceptionType exceptionType = null;
        if (params.get("exceptionType") != null) {
            exceptionType = ExceptionType.valueOf(params.get("exceptionType").toString());
        }
        String exceptionDescription = (String) params.get("exceptionDescription");
        Long confirmerId = currentUserUtil.getCurrentUserId();
        return Result.success("归还成功", borrowService.returnItemWithException(
                id, returnRemark, confirmerId, hasException, exceptionType, exceptionDescription));
    }

    @PostMapping("/{id}/confirm-exception")
    @Operation(summary = "确认异常", description = "确认异常归还情况")
    public Result<BorrowRecord> confirmException(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String confirmRemark = params.get("confirmRemark");
        return Result.success("确认成功", borrowService.confirmException(id, confirmRemark));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消借用申请", description = "取消待确认的借用申请")
    public Result<Void> cancelBorrow(@PathVariable Long id) {
        borrowService.cancelBorrow(id);
        return Result.successMsg("取消成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取借用记录详情", description = "根据ID获取借用记录详情")
    public Result<BorrowRecord> getRecordById(@PathVariable Long id) {
        return Result.success(borrowService.getRecordById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询借用记录", description = "分页查询借用记录，支持多种筛选条件")
    public Result<Page<BorrowRecord>> listRecords(
            @Parameter(description = "物品ID") @RequestParam(required = false) Long itemId,
            @Parameter(description = "借用人ID") @RequestParam(required = false) Long borrowerId,
            @Parameter(description = "借用状态") @RequestParam(required = false) BorrowStatus status,
            @Parameter(description = "异常类型") @RequestParam(required = false) ExceptionType exceptionType,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return Result.success(borrowService.listRecords(itemId, borrowerId, status, exceptionType, startTime, endTime, pageable));
    }

    @GetMapping("/my")
    @Operation(summary = "我的借用记录", description = "获取当前用户的借用记录")
    public Result<Page<BorrowRecord>> listMyRecords(
            @Parameter(description = "借用状态") @RequestParam(required = false) BorrowStatus status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Long myId = currentUserUtil.getCurrentUserId();
        return Result.success(borrowService.listRecords(null, myId, status, null, null, null, pageable));
    }
}
