package com.logistics.controller;

import com.logistics.dto.ApiResponse;
import com.logistics.dto.CategoryRequest;
import com.logistics.entity.Category;
import com.logistics.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "物品分类CRUD接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping
    public ApiResponse<List<Category>> findAll() {
        return ApiResponse.ok(categoryService.findAll());
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public ApiResponse<Category> findById(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.findById(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public ApiResponse<Category> create(@RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.create(request));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.update(id, request));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok();
    }
}
