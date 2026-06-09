package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.ItemLocation;
import com.logistics.itemlending.service.ItemLocationService;
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

@RestController
@RequestMapping("/item-locations")
@Tag(name = "存放位置管理", description = "物品存放位置管理相关接口")
public class ItemLocationController {

    @Autowired
    private ItemLocationService itemLocationService;

    @PostMapping
    @Operation(summary = "创建存放位置", description = "创建新的物品存放位置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ItemLocation> createLocation(@RequestBody ItemLocation location) {
        return Result.success("创建成功", itemLocationService.createLocation(location));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新存放位置", description = "更新物品存放位置信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ItemLocation> updateLocation(@PathVariable Long id, @RequestBody ItemLocation location) {
        return Result.success("更新成功", itemLocationService.updateLocation(id, location));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除存放位置", description = "删除物品存放位置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteLocation(@PathVariable Long id) {
        itemLocationService.deleteLocation(id);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取位置详情", description = "根据ID获取存放位置详情")
    public Result<ItemLocation> getLocationById(@PathVariable Long id) {
        return Result.success(itemLocationService.getLocationById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询位置列表", description = "分页查询存放位置列表")
    public Result<Page<ItemLocation>> listLocations(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder"));
        return Result.success(itemLocationService.listLocations(keyword, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有位置", description = "获取所有启用的存放位置列表")
    public Result<List<ItemLocation>> listAllLocations() {
        return Result.success(itemLocationService.listAllLocations());
    }
}
