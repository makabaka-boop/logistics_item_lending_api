package com.gsb.logistics.web;

import com.gsb.logistics.domain.Item;
import com.gsb.logistics.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@Tag(name = "4.物品建档", description = "物品建档/可借数量/责任人维护与查询")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) { this.service = service; }

    @Operation(summary = "物品列表/筛选",
            description = "支持按分类、位置、责任人、关键词、是否库存预警筛选")
    @GetMapping
    public ApiResponse<List<Item>> list(
            @Parameter(description = "分类 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "存放位置 ID") @RequestParam(required = false) Long locationId,
            @Parameter(description = "关键词（名称/编码）") @RequestParam(required = false) String keyword,
            @Parameter(description = "责任人") @RequestParam(required = false) String owner,
            @Parameter(description = "是否仅显示库存预警") @RequestParam(required = false) Boolean lowStock) {
        return ApiResponse.ok(service.search(categoryId, locationId, keyword, owner, lowStock));
    }

    @Operation(summary = "查询物品详情")
    @GetMapping("/{id}")
    public ApiResponse<Item> get(@PathVariable Long id) { return ApiResponse.ok(service.get(id)); }

    @Operation(summary = "新增物品")
    @PostMapping
    public ApiResponse<Item> create(@RequestBody Item it) { return ApiResponse.ok(service.create(it)); }

    @Operation(summary = "更新物品")
    @PutMapping("/{id}")
    public ApiResponse<Item> update(@PathVariable Long id, @RequestBody Item it) {
        return ApiResponse.ok(service.update(id, it));
    }

    @Operation(summary = "删除物品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
