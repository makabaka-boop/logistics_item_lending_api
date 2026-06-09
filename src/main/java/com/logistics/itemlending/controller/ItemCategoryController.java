package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.ItemCategory;
import com.logistics.itemlending.service.ItemCategoryService;
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
@RequestMapping("/item-categories")
@Tag(name = "物品分类管理", description = "物品分类管理相关接口")
public class ItemCategoryController {

    @Autowired
    private ItemCategoryService itemCategoryService;

    @PostMapping
    @Operation(summary = "创建物品分类", description = "创建新的物品分类")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ItemCategory> createCategory(@RequestBody ItemCategory category) {
        return Result.success("创建成功", itemCategoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新物品分类", description = "更新物品分类信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ItemCategory> updateCategory(@PathVariable Long id, @RequestBody ItemCategory category) {
        return Result.success("更新成功", itemCategoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物品分类", description = "删除物品分类")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        itemCategoryService.deleteCategory(id);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据ID获取物品分类详情")
    public Result<ItemCategory> getCategoryById(@PathVariable Long id) {
        return Result.success(itemCategoryService.getCategoryById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询分类列表", description = "分页查询物品分类列表")
    public Result<Page<ItemCategory>> listCategories(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder"));
        return Result.success(itemCategoryService.listCategories(keyword, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有分类", description = "获取所有启用的物品分类列表")
    public Result<List<ItemCategory>> listAllCategories() {
        return Result.success(itemCategoryService.listAllCategories());
    }
}
