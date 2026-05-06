package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.entity.*;
import com.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计服务
 */
@Service
public class StatisticsService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private PracticeRecordMapper practiceRecordMapper;
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;
    
    /**
     * 获取管理员仪表盘统计
     */
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        stats.put("userCount", userMapper.selectCount(null));
        
        // 分类总数
        stats.put("categoryCount", categoryMapper.selectCount(null));
        
        // 题目总数
        stats.put("questionCount", questionMapper.selectCount(null));
        
        // 试卷总数
        stats.put("examCount", examMapper.selectCount(null));
        
        // 练习记录总数
        stats.put("practiceCount", practiceRecordMapper.selectCount(null));
        
        // 考试记录总数
        stats.put("examRecordCount", examRecordMapper.selectCount(null));
        
        return stats;
    }
    
    /**
     * 获取用户仪表盘统计
     */
    public Map<String, Object> getUserDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 练习次数
        long practiceCount = practiceRecordMapper.selectCount(
                new LambdaQueryWrapper<PracticeRecord>().eq(PracticeRecord::getUserId, userId));
        stats.put("practiceCount", practiceCount);
        
        // 考试次数
        long examCount = examRecordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, userId));
        stats.put("examCount", examCount);
        
        // 计算考试平均分
        if (examCount > 0) {
            java.util.List<ExamRecord> records = examRecordMapper.selectList(
                    new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getUserId, userId)
                            .eq(ExamRecord::getStatus, "COMPLETED"));
            if (records != null && !records.isEmpty()) {
                double totalScore = records.stream().mapToInt(ExamRecord::getScore).sum();
                double avgScore = Math.round(totalScore / records.size() * 10) / 10.0;
                stats.put("avgScore", avgScore);
            } else {
                stats.put("avgScore", 0);
            }
        } else {
            stats.put("avgScore", 0);
        }
        
        // 错题数量
        long wrongCount = wrongQuestionMapper.selectCount(
                new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getUserId, userId));
        stats.put("wrongCount", wrongCount);
        
        // 可参加考试数量
        long availableExamCount = examMapper.selectCount(
                new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 1));
        stats.put("availableExamCount", availableExamCount);
        
        // 分类数量
        long categoryCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1));
        stats.put("categoryCount", categoryCount);
        
        return stats;
    }
}
