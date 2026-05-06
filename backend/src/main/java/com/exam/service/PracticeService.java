package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.AnswerDTO;
import com.exam.dto.PracticeSubmitDTO;
import com.exam.entity.*;
import com.exam.exception.BusinessException;
import com.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 练习服务
 */
@Service
public class PracticeService {
    
    @Autowired
    private PracticeRecordMapper practiceRecordMapper;
    
    @Autowired
    private PracticeAnswerMapper practiceAnswerMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionOptionMapper questionOptionMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    /**
     * 获取练习题目
     */
    public List<Question> getPracticeQuestions(Long categoryId, Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }
        
        List<Question> questions = questionMapper.selectRandomByCategory(categoryId, count);
        
        // 填充选项
        questions.forEach(q -> {
            if (q.getType().equals(Constants.QUESTION_SINGLE) || 
                    q.getType().equals(Constants.QUESTION_MULTIPLE)) {
                q.setOptions(questionOptionMapper.selectByQuestionId(q.getId()));
            }
        });
        
        return questions;
    }
    
    /**
     * 提交练习
     */
    @Transactional
    public PracticeRecord submitPractice(Long userId, PracticeSubmitDTO dto) {
        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new BusinessException("答案列表不能为空");
        }
        
        // 创建练习记录
        PracticeRecord record = new PracticeRecord();
        record.setUserId(userId);
        record.setCategoryId(dto.getCategoryId());
        record.setMode(dto.getMode() != null ? dto.getMode() : Constants.PRACTICE_MODE_NORMAL);
        record.setTotalCount(dto.getAnswers().size());
        
        int correctCount = 0;
        int totalScore = 0;
        
        practiceRecordMapper.insert(record);
        
        // 保存答案并判分
        for (AnswerDTO answer : dto.getAnswers()) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;
            
            boolean isCorrect = checkAnswer(question, answer.getAnswer());
            
            PracticeAnswer pa = new PracticeAnswer();
            pa.setRecordId(record.getId());
            pa.setQuestionId(answer.getQuestionId());
            pa.setUserAnswer(answer.getAnswer());
            pa.setIsCorrect(isCorrect);
            practiceAnswerMapper.insert(pa);
            
            if (isCorrect) {
                correctCount++;
                totalScore += question.getScore();
                
                // 如果是错题模式且答对了，从错题本删除
                if (Constants.PRACTICE_MODE_WRONG.equals(dto.getMode())) {
                    wrongQuestionService.removeWrongQuestion(userId, answer.getQuestionId());
                }
            } else {
                // 答错了，添加到错题本
                wrongQuestionService.addWrongQuestion(userId, answer.getQuestionId(), 
                        Constants.WRONG_SOURCE_PRACTICE, record.getId());
            }
        }
        
        // 更新练习记录
        record.setCorrectCount(correctCount);
        record.setScore(totalScore);
        practiceRecordMapper.updateById(record);
        
        return record;
    }
    
    /**
     * 获取用户练习记录
     */
    public IPage<PracticeRecord> getUserPracticeRecords(Long userId, int page, int size) {
        LambdaQueryWrapper<PracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeRecord::getUserId, userId)
                .orderByDesc(PracticeRecord::getCreatedAt);
        
        IPage<PracticeRecord> result = practiceRecordMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 填充分类名称
        result.getRecords().forEach(r -> {
            Category category = categoryMapper.selectById(r.getCategoryId());
            if (category != null) {
                r.setCategoryName(category.getName());
            }
        });
        
        return result;
    }
    
    /**
     * 获取练习记录详情
     */
    public PracticeRecord getPracticeRecordDetail(Long recordId) {
        PracticeRecord record = practiceRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("练习记录不存在");
        }
        
        // 填充分类名称
        Category category = categoryMapper.selectById(record.getCategoryId());
        if (category != null) {
            record.setCategoryName(category.getName());
        }
        
        // 填充答案详情
        List<PracticeAnswer> answers = practiceAnswerMapper.selectList(
                new LambdaQueryWrapper<PracticeAnswer>().eq(PracticeAnswer::getRecordId, recordId));
        
        answers.forEach(a -> {
            Question question = questionMapper.selectById(a.getQuestionId());
            if (question != null) {
                if (question.getType().equals(Constants.QUESTION_SINGLE) || 
                        question.getType().equals(Constants.QUESTION_MULTIPLE)) {
                    question.setOptions(questionOptionMapper.selectByQuestionId(question.getId()));
                }
                a.setQuestion(question);
            }
        });
        
        record.setAnswers(answers);
        return record;
    }
    
    /**
     * 检查答案是否正确
     */
    private boolean checkAnswer(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }
        
        String correctAnswer = question.getAnswer();
        
        switch (question.getType()) {
            case Constants.QUESTION_SINGLE:
            case Constants.QUESTION_JUDGE:
                return correctAnswer.equalsIgnoreCase(userAnswer.trim());
                
            case Constants.QUESTION_MULTIPLE:
                // 多选题：答案排序后比较
                String[] correctOptions = correctAnswer.split(",");
                String[] userOptions = userAnswer.split(",");
                Arrays.sort(correctOptions);
                Arrays.sort(userOptions);
                return Arrays.equals(correctOptions, userOptions);
                
            case Constants.QUESTION_FILL:
                // 填空题：支持多个答案，按顺序匹配
                String[] correctFills = correctAnswer.split(",");
                String[] userFills = userAnswer.split(",");
                if (correctFills.length != userFills.length) {
                    return false;
                }
                for (int i = 0; i < correctFills.length; i++) {
                    if (!correctFills[i].trim().equalsIgnoreCase(userFills[i].trim())) {
                        return false;
                    }
                }
                return true;
                
            case Constants.QUESTION_ESSAY:
                // 问答题：需要手动评分，这里简单判断是否包含关键词
                // 实际应用中应该标记为待评分
                return false;
                
            default:
                return false;
        }
    }
}
