package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.PracticeSubmitDTO;
import com.exam.entity.PracticeRecord;
import com.exam.entity.Question;
import com.exam.entity.User;
import com.exam.service.PracticeService;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 练习控制器
 */
@RestController
@RequestMapping("/api/practice")
public class PracticeController {
    
    @Autowired
    private PracticeService practiceService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取练习题目
     */
    @GetMapping("/questions")
    public Result<List<Question>> getPracticeQuestions(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "10") Integer count) {
        List<Question> questions = practiceService.getPracticeQuestions(categoryId, count);
        // 隐藏正确答案
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setAnalysis(null);
        });
        return Result.success(questions);
    }
    
    /**
     * 提交练习
     */
    @PostMapping("/submit")
    public Result<PracticeRecord> submitPractice(@RequestBody PracticeSubmitDTO dto) {
        User currentUser = userService.getCurrentUser();
        PracticeRecord record = practiceService.submitPractice(currentUser.getId(), dto);
        return Result.success("提交成功", record);
    }
    
    /**
     * 获取练习记录列表
     */
    @GetMapping("/records")
    public Result<PageResult<PracticeRecord>> getPracticeRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        return Result.success(PageResult.of(
                practiceService.getUserPracticeRecords(currentUser.getId(), page, size)));
    }
    
    /**
     * 获取练习记录详情
     */
    @GetMapping("/records/{id}")
    public Result<PracticeRecord> getPracticeRecordDetail(@PathVariable Long id) {
        return Result.success(practiceService.getPracticeRecordDetail(id));
    }
}
