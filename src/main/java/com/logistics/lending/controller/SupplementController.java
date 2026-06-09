package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.dto.SupplementApproveRequest;
import com.logistics.lending.dto.SupplementRequest;
import com.logistics.lending.entity.StockSupplement;
import com.logistics.lending.service.SupplementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplements")
@RequiredArgsConstructor
@Tag(name = "库存补充", description = "补充申请提交、审批")
public class SupplementController {

    private final SupplementService supplementService;

    @PostMapping
    @Operation(summary = "提交补充申请")
    public ApiResponse<StockSupplement> submit(@Valid @RequestBody SupplementRequest request) {
        return ApiResponse.success(supplementService.submit(request));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审批补充申请", description = "审批通过后增加库存")
    public ApiResponse<StockSupplement> approve(@PathVariable Long id, @RequestBody SupplementApproveRequest request) {
        return ApiResponse.success(supplementService.approve(id, request));
    }

    @GetMapping
    @Operation(summary = "获取补充申请列表")
    public ApiResponse<List<StockSupplement>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(supplementService.list(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取补充申请详情")
    public ApiResponse<StockSupplement> getById(@PathVariable Long id) {
        return ApiResponse.success(supplementService.getById(id));
    }
}
