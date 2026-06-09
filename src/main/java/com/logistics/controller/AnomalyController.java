package com.logistics.controller;

import com.logistics.dto.*;
import com.logistics.entity.AnomalyRecord;
import com.logistics.service.AnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "异常管理", description = "异常上报、确认接口")
@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyService anomalyService;

    @Operation(summary = "异常上报")
    @PostMapping
    public ApiResponse<AnomalyRecord> create(@RequestBody AnomalyRequest request) {
        return ApiResponse.ok(anomalyService.create(request));
    }

    @Operation(summary = "异常确认")
    @PutMapping("/{id}/confirm")
    public ApiResponse<AnomalyRecord> confirm(@PathVariable Long id, @RequestBody AnomalyConfirmRequest request) {
        return ApiResponse.ok(anomalyService.confirm(id, request));
    }

    @Operation(summary = "查询异常记录")
    @PostMapping("/query")
    public ApiResponse<List<AnomalyRecord>> query(@RequestBody AnomalyQueryRequest request) {
        return ApiResponse.ok(anomalyService.query(request));
    }
}
