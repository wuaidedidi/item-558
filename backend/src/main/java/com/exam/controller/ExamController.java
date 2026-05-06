package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.ExamDTO;
import com.exam.entity.Exam;
import com.exam.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 试卷控制器
 */
@RestController
@RequestMapping("/api")
public class ExamController {
    
    @Autowired
    private ExamService examService;
    
    /**
     * 获取已发布的试卷列表（用户端）
     */
    @GetMapping("/exams")
    public Result<List<Exam>> getPublishedExams() {
        return Result.success(examService.getPublishedExams());
    }
    
    /**
     * 获取试卷详情（用户端，用于考试）
     */
    @GetMapping("/exams/{id}")
    public Result<Exam> getExamForUser(@PathVariable Long id) {
        Exam exam = examService.getExamWithQuestions(id);
        // 隐藏正确答案
        if (exam.getQuestions() != null) {
            exam.getQuestions().forEach(eq -> {
                if (eq.getQuestion() != null) {
                    eq.getQuestion().setAnswer(null);
                    eq.getQuestion().setAnalysis(null);
                }
            });
        }
        return Result.success(exam);
    }
    
    /**
     * 获取试卷列表（管理员）
     */
    @GetMapping("/admin/exams")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Exam>> getExamList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(examService.getExamList(page, size, status, keyword)));
    }
    
    /**
     * 获取试卷详情（管理员）
     */
    @GetMapping("/admin/exams/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Exam> getExamById(@PathVariable Long id) {
        return Result.success(examService.getExamWithQuestions(id));
    }
    
    /**
     * 创建试卷
     */
    @PostMapping("/admin/exams")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createExam(@Valid @RequestBody ExamDTO dto) {
        examService.createExam(dto);
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新试卷
     */
    @PutMapping("/admin/exams/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateExam(@PathVariable Long id, @Valid @RequestBody ExamDTO dto) {
        examService.updateExam(id, dto);
        return Result.success("更新成功", null);
    }
    
    /**
     * 发布试卷
     */
    @PutMapping("/admin/exams/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publishExam(@PathVariable Long id) {
        examService.publishExam(id);
        return Result.success("发布成功", null);
    }
    
    /**
     * 结束试卷
     */
    @PutMapping("/admin/exams/{id}/end")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> endExam(@PathVariable Long id) {
        examService.endExam(id);
        return Result.success("已结束", null);
    }
    
    /**
     * 删除试卷
     */
    @DeleteMapping("/admin/exams/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return Result.success("删除成功", null);
    }
}
