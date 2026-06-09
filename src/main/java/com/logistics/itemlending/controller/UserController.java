package com.logistics.itemlending.controller;

import com.logistics.itemlending.common.Result;
import com.logistics.itemlending.entity.User;
import com.logistics.itemlending.enums.UserRole;
import com.logistics.itemlending.service.UserService;
import com.logistics.itemlending.util.CurrentUserUtil;
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
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CurrentUserUtil currentUserUtil;

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户，仅管理员可操作")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@RequestBody User user) {
        return Result.success("创建成功", userService.createUser(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return Result.success("更新成功", userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除用户，仅管理员可操作")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详情")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息")
    public Result<User> getCurrentUser() {
        return Result.success(currentUserUtil.getCurrentUser());
    }

    @PutMapping("/me/password")
    @Operation(summary = "修改当前用户密码", description = "修改当前登录用户的密码")
    public Result<Void> updateMyPassword(@RequestBody Map<String, String> passwordMap) {
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        Long userId = currentUserUtil.getCurrentUserId();
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.successMsg("密码修改成功");
    }

    @GetMapping
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持关键词和角色筛选")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> listUsers(
            @Parameter(description = "搜索关键词（用户名/真实姓名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户角色") @RequestParam(required = false) UserRole role,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return Result.success(userService.listUsers(keyword, role, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有启用用户", description = "获取所有启用的用户列表，用于选择责任人等")
    public Result<List<User>> listAllUsers() {
        return Result.success(userService.listAllUsers());
    }
}
