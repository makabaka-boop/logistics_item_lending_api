package com.logistics.controller;

import com.logistics.dto.ApiResponse;
import com.logistics.dto.ItemQueryRequest;
import com.logistics.dto.ItemRequest;
import com.logistics.dto.ReplenishRequest;
import com.logistics.entity.Item;
import com.logistics.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "物品管理", description = "物品建档、查询、补充接口")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "获取所有物品")
    @GetMapping
    public ApiResponse<List<Item>> findAll() {
        return ApiResponse.ok(itemService.findAll());
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/{id}")
    public ApiResponse<Item> findById(@PathVariable Long id) {
        return ApiResponse.ok(itemService.findById(id));
    }

    @Operation(summary = "条件查询物品")
    @PostMapping("/query")
    public ApiResponse<List<Item>> query(@RequestBody ItemQueryRequest request) {
        return ApiResponse.ok(itemService.query(request));
    }

    @Operation(summary = "物品建档")
    @PostMapping
    public ApiResponse<Item> create(@RequestBody ItemRequest request) {
        return ApiResponse.ok(itemService.create(request));
    }

    @Operation(summary = "更新物品")
    @PutMapping("/{id}")
    public ApiResponse<Item> update(@PathVariable Long id, @RequestBody ItemRequest request) {
        return ApiResponse.ok(itemService.update(id, request));
    }

    @Operation(summary = "补充申请")
    @PostMapping("/{id}/replenish")
    public ApiResponse<Item> replenish(@PathVariable Long id, @RequestBody ReplenishRequest request) {
        return ApiResponse.ok(itemService.replenish(id, request));
    }
}
