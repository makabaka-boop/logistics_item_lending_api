package com.gsb.logistics.web;

import com.gsb.logistics.domain.Category;
import com.gsb.logistics.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "2.物品分类", description = "维护物品分类")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) { this.service = service; }

    @Operation(summary = "分类列表")
    @GetMapping
    public ApiResponse<List<Category>> list() { return ApiResponse.ok(service.list()); }

    @Operation(summary = "新增分类")
    @PostMapping
    public ApiResponse<Category> create(@RequestBody Category c) { return ApiResponse.ok(service.create(c)); }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id, @RequestBody Category c) {
        return ApiResponse.ok(service.update(id, c));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
