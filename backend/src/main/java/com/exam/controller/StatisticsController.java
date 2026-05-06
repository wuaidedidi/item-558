package com.exam.controller;

import com.exam.common.Result;
import com.exam.entity.User;
import com.exam.service.StatisticsService;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取管理员仪表盘统计
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getAdminStats() {
        return Result.success(statisticsService.getAdminDashboardStats());
    }
    
    /**
     * 获取用户仪表盘统计
     */
    @GetMapping("/user/statistics")
    public Result<Map<String, Object>> getUserStats() {
        User currentUser = userService.getCurrentUser();
        return Result.success(statisticsService.getUserDashboardStats(currentUser.getId()));
    }
}
