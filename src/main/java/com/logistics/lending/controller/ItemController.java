package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.dto.ItemRequest;
import com.logistics.lending.entity.Item;
import com.logistics.lending.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "物品管理", description = "物品建档、查询、修改")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "获取物品列表", description = "可按分类ID、位置ID筛选")
    public ApiResponse<List<Item>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId) {
        return ApiResponse.success(itemService.list(categoryId, locationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取物品详情")
    public ApiResponse<Item> getById(@PathVariable Long id) {
        return ApiResponse.success(itemService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建物品（建档）")
    public ApiResponse<Item> create(@Valid @RequestBody ItemRequest request) {
        return ApiResponse.success(itemService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新物品信息")
    public ApiResponse<Item> update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ApiResponse.success(itemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物品")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ApiResponse.success(null);
    }
}
