package com.gsb.logistics.web;

import com.gsb.logistics.domain.Location;
import com.gsb.logistics.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "3.存放位置", description = "维护存放位置")
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) { this.service = service; }

    @Operation(summary = "位置列表")
    @GetMapping
    public ApiResponse<List<Location>> list() { return ApiResponse.ok(service.list()); }

    @Operation(summary = "新增位置")
    @PostMapping
    public ApiResponse<Location> create(@RequestBody Location l) { return ApiResponse.ok(service.create(l)); }

    @Operation(summary = "更新位置")
    @PutMapping("/{id}")
    public ApiResponse<Location> update(@PathVariable Long id, @RequestBody Location l) {
        return ApiResponse.ok(service.update(id, l));
    }

    @Operation(summary = "删除位置")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
