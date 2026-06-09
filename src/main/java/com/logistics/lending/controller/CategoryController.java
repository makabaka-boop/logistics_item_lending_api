package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.dto.CategoryRequest;
import com.logistics.lending.entity.Category;
import com.logistics.lending.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "物品分类管理", description = "物品分类增删改查")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "获取分类列表")
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.list());
    }

    @PostMapping
    @Operation(summary = "创建分类")
    public ApiResponse<Category> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public ApiResponse<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success(null);
    }
}
