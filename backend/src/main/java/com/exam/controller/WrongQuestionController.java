package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.AnswerDTO;
import com.exam.entity.Question;
import com.exam.entity.User;
import com.exam.entity.WrongQuestion;
import com.exam.service.UserService;
import com.exam.service.WrongQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 错题本控制器
 */
@RestController
@RequestMapping("/api/wrong")
public class WrongQuestionController {
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取错题列表
     */
    @GetMapping("/list")
    public Result<PageResult<WrongQuestion>> getWrongQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        return Result.success(PageResult.of(
                wrongQuestionService.getUserWrongQuestions(currentUser.getId(), page, size)));
    }
    
    /**
     * 获取错题练习题目
     */
    @GetMapping("/practice")
    public Result<List<Question>> getWrongPracticeQuestions(
            @RequestParam(defaultValue = "10") Integer count) {
        User currentUser = userService.getCurrentUser();
        List<Question> questions = wrongQuestionService.getWrongPracticeQuestions(currentUser.getId(), count);
        // 隐藏正确答案
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setAnalysis(null);
        });
        return Result.success(questions);
    }
    
    /**
     * 提交错题练习
     */
    @PostMapping("/practice/submit")
    public Result<Map<String, Object>> submitWrongPractice(@RequestBody List<AnswerDTO> answers) {
        User currentUser = userService.getCurrentUser();
        int correctCount = wrongQuestionService.submitWrongPractice(currentUser.getId(), answers);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", answers.size());
        result.put("correctCount", correctCount);
        result.put("removedCount", correctCount); // 做对的题目数即为删除的题目数
        
        return Result.success("提交成功", result);
    }
    
    /**
     * 获取错题数量
     */
    @GetMapping("/count")
    public Result<Long> getWrongQuestionCount() {
        User currentUser = userService.getCurrentUser();
        return Result.success(wrongQuestionService.getUserWrongQuestionCount(currentUser.getId()));
    }
    
    /**
     * 清空错题本
     */
    @DeleteMapping("/clear")
    public Result<Void> clearWrongQuestions() {
        User currentUser = userService.getCurrentUser();
        wrongQuestionService.clearWrongQuestions(currentUser.getId());
        return Result.success("清空成功", null);
    }
    
    /**
     * 删除单个错题
     */
    @DeleteMapping("/{questionId}")
    public Result<Void> removeWrongQuestion(@PathVariable Long questionId) {
        User currentUser = userService.getCurrentUser();
        wrongQuestionService.removeWrongQuestion(currentUser.getId(), questionId);
        return Result.success("删除成功", null);
    }
}
