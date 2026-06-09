package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.Item;
import com.logistics.itemlending.enums.ItemStatus;
import com.logistics.itemlending.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@Tag(name = "物品管理", description = "物品信息管理相关接口")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    @Operation(summary = "创建物品", description = "创建新的物品档案")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Item> createItem(@RequestBody Item item) {
        return Result.success("创建成功", itemService.createItem(item));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新物品", description = "更新物品信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        return Result.success("更新成功", itemService.updateItem(id, item));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物品", description = "删除物品档案")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取物品详情", description = "根据ID获取物品详情")
    public Result<Item> getItemById(@PathVariable Long id) {
        return Result.success(itemService.getItemById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询物品列表", description = "分页查询物品列表，支持多种筛选条件")
    public Result<Page<Item>> listItems(
            @Parameter(description = "搜索关键词（编码/名称）") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "位置ID") @RequestParam(required = false) Long locationId,
            @Parameter(description = "责任人ID") @RequestParam(required = false) Long responsiblePersonId,
            @Parameter(description = "物品状态") @RequestParam(required = false) ItemStatus status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return Result.success(itemService.listItems(keyword, categoryId, locationId, responsiblePersonId, status, pageable));
    }

    @PostMapping("/{id}/add-stock")
    @Operation(summary = "补充库存", description = "补充物品库存")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Item> addStock(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer quantity = (Integer) params.get("quantity");
        String remark = (String) params.get("remark");
        return Result.success("补充成功", itemService.addStock(id, quantity, remark));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "获取库存不足物品", description = "获取所有库存不足的物品列表")
    public Result<List<Item>> listLowStockItems() {
        return Result.success(itemService.listLowStockItems());
    }
}
