package com.logistics.controller;

import com.logistics.dto.ApiResponse;
import com.logistics.dto.LocationRequest;
import com.logistics.entity.Location;
import com.logistics.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "位置管理", description = "存放位置CRUD接口")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "获取所有位置")
    @GetMapping
    public ApiResponse<List<Location>> findAll() {
        return ApiResponse.ok(locationService.findAll());
    }

    @Operation(summary = "获取位置详情")
    @GetMapping("/{id}")
    public ApiResponse<Location> findById(@PathVariable Long id) {
        return ApiResponse.ok(locationService.findById(id));
    }

    @Operation(summary = "创建位置")
    @PostMapping
    public ApiResponse<Location> create(@RequestBody LocationRequest request) {
        return ApiResponse.ok(locationService.create(request));
    }

    @Operation(summary = "更新位置")
    @PutMapping("/{id}")
    public ApiResponse<Location> update(@PathVariable Long id, @RequestBody LocationRequest request) {
        return ApiResponse.ok(locationService.update(id, request));
    }

    @Operation(summary = "删除位置")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ApiResponse.ok();
    }
}
