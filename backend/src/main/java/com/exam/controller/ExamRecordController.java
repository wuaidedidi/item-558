package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.ExamSubmitDTO;
import com.exam.entity.ExamRecord;
import com.exam.entity.User;
import com.exam.service.ExamRecordService;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 考试记录控制器
 */
@RestController
@RequestMapping("/api")
public class ExamRecordController {
    
    @Autowired
    private ExamRecordService examRecordService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 开始考试
     */
    @PostMapping("/exam/start")
    public Result<ExamRecord> startExam(@RequestParam Long examId) {
        User currentUser = userService.getCurrentUser();
        ExamRecord record = examRecordService.startExam(currentUser.getId(), examId);
        return Result.success(record);
    }
    
    /**
     * 提交考试
     */
    @PostMapping("/exam/submit")
    public Result<ExamRecord> submitExam(@RequestBody ExamSubmitDTO dto) {
        User currentUser = userService.getCurrentUser();
        ExamRecord record = examRecordService.submitExam(currentUser.getId(), dto);
        return Result.success("提交成功", record);
    }
    
    /**
     * 获取用户考试记录
     */
    @GetMapping("/exam/records")
    public Result<PageResult<ExamRecord>> getUserExamRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        return Result.success(PageResult.of(
                examRecordService.getUserExamRecords(currentUser.getId(), page, size)));
    }
    
    /**
     * 获取考试记录详情
     */
    @GetMapping("/exam/records/{id}")
    public Result<ExamRecord> getExamRecordDetail(@PathVariable Long id) {
        return Result.success(examRecordService.getExamRecordDetail(id));
    }
    
    /**
     * 获取所有考试记录（管理员）
     */
    @GetMapping("/admin/exam-records")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ExamRecord>> getAllExamRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long examId) {
        return Result.success(PageResult.of(
                examRecordService.getAllExamRecords(page, size, examId)));
    }
}
