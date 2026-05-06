package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Question;
import com.exam.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/api")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    /**
     * 获取题目列表（管理员）
     */
    @GetMapping("/admin/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Question>> getQuestionList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(
                questionService.getQuestionList(page, size, categoryId, type, difficulty, keyword)));
    }
    
    /**
     * 获取所有启用的题目（用于选题）
     */
    @GetMapping("/admin/questions/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Question>> getAllQuestions(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type) {
        return Result.success(questionService.getAllEnabledQuestions(categoryId, type));
    }
    
    /**
     * 获取题目详情
     */
    @GetMapping("/admin/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Question> getQuestionById(@PathVariable Long id) {
        return Result.success(questionService.getQuestionById(id));
    }
    
    /**
     * 创建题目
     */
    @PostMapping("/admin/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createQuestion(@Valid @RequestBody QuestionDTO dto) {
        questionService.createQuestion(dto);
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新题目
     */
    @PutMapping("/admin/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        questionService.updateQuestion(id, dto);
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除题目
     */
    @DeleteMapping("/admin/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success("删除成功", null);
    }
    
    /**
     * 批量删除题目
     */
    @DeleteMapping("/admin/questions/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDeleteQuestions(@RequestBody List<Long> ids) {
        questionService.batchDeleteQuestions(ids);
        return Result.success("批量删除成功", null);
    }
}
