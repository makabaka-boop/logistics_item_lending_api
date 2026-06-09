package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.dto.LocationRequest;
import com.logistics.lending.entity.StorageLocation;
import com.logistics.lending.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "存放位置管理", description = "存放位置增删改查")
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    @Operation(summary = "获取位置列表")
    public ApiResponse<List<StorageLocation>> list() {
        return ApiResponse.success(locationService.list());
    }

    @PostMapping
    @Operation(summary = "创建位置")
    public ApiResponse<StorageLocation> create(@Valid @RequestBody LocationRequest request) {
        return ApiResponse.success(locationService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新位置")
    public ApiResponse<StorageLocation> update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return ApiResponse.success(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除位置")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ApiResponse.success(null);
    }
}
