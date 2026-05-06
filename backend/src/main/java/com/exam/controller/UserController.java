package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.PasswordChangeDTO;
import com.exam.dto.UserUpdateDTO;
import com.exam.entity.User;
import com.exam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/profile")
    public Result<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 更新当前用户信息
     */
    @PutMapping("/user/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        User currentUser = userService.getCurrentUser();
        userService.updateUser(currentUser.getId(), dto);
        return Result.success("更新成功", null);
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/user/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto) {
        User currentUser = userService.getCurrentUser();
        userService.changePassword(currentUser.getId(), dto);
        return Result.success("密码修改成功", null);
    }
    
    /**
     * 获取用户列表（管理员）
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(userService.getUserList(page, size, keyword)));
    }
    
    /**
     * 获取用户详情（管理员）
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }
    
    /**
     * 更新用户状态（管理员）
     */
    @PutMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success("状态更新成功", null);
    }
    
    /**
     * 更新用户角色（管理员）
     */
    @PutMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.updateUserRole(id, role);
        return Result.success("角色更新成功", null);
    }
    
    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }
}
