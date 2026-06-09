package com.logistics.controller;

import com.logistics.dto.*;
import com.logistics.entity.RepairRecord;
import com.logistics.service.RepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "维修管理", description = "维修登记、流转、复核接口")
@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    @Operation(summary = "维修登记")
    @PostMapping
    public ApiResponse<RepairRecord> create(@RequestBody RepairRequest request) {
        return ApiResponse.ok(repairService.create(request));
    }

    @Operation(summary = "开始维修")
    @PutMapping("/{id}/start")
    public ApiResponse<RepairRecord> startRepair(@PathVariable Long id, @RequestBody RepairActionRequest request) {
        return ApiResponse.ok(repairService.startRepair(id, request));
    }

    @Operation(summary = "维修完成")
    @PutMapping("/{id}/complete")
    public ApiResponse<RepairRecord> completeRepair(@PathVariable Long id, @RequestBody RepairActionRequest request) {
        return ApiResponse.ok(repairService.completeRepair(id, request));
    }

    @Operation(summary = "维修复核")
    @PutMapping("/{id}/review")
    public ApiResponse<RepairRecord> review(@PathVariable Long id, @RequestBody RepairReviewRequest request) {
        return ApiResponse.ok(repairService.review(id, request));
    }

    @Operation(summary = "查询维修记录")
    @PostMapping("/query")
    public ApiResponse<List<RepairRecord>> query(@RequestBody RepairQueryRequest request) {
        return ApiResponse.ok(repairService.query(request));
    }
}
