package com.logistics.lending.controller;

import com.logistics.lending.dto.ApiResponse;
import com.logistics.lending.dto.UserCreateRequest;
import com.logistics.lending.entity.User;
import com.logistics.lending.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户增删改查（需要ADMIN角色）")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "获取用户列表")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userService.listUsers());
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public ApiResponse<User> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @PutMapping("/{id}/toggle-enabled")
    @Operation(summary = "启用/禁用用户")
    public ApiResponse<User> toggleEnabled(@PathVariable Long id) {
        return ApiResponse.success(userService.toggleEnabled(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }
}
