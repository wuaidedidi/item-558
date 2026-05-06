package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.Category;
import com.exam.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 获取所有启用的分类（用户端）
     */
    @GetMapping("/categories")
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryService.getAllEnabledCategories());
    }
    
    /**
     * 获取分类列表（管理员）
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Category>> getCategoryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(categoryService.getCategoryList(page, size, keyword)));
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }
    
    /**
     * 创建分类
     */
    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createCategory(@Valid @RequestBody Category category) {
        categoryService.createCategory(category);
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        categoryService.updateCategory(id, category);
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
