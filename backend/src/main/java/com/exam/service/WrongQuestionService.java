package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.AnswerDTO;
import com.exam.entity.Question;
import com.exam.entity.WrongQuestion;
import com.exam.exception.BusinessException;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.mapper.WrongQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题本服务
 */
@Service
public class WrongQuestionService {
    
    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionOptionMapper questionOptionMapper;
    
    /**
     * 添加错题
     */
    @Transactional
    public void addWrongQuestion(Long userId, Long questionId, String sourceType, Long sourceId) {
        // 检查是否已存在
        WrongQuestion existing = wrongQuestionMapper.selectOne(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getUserId, userId)
                        .eq(WrongQuestion::getQuestionId, questionId));
        
        if (existing != null) {
            // 更新错误次数
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setSourceType(sourceType);
            existing.setSourceId(sourceId);
            wrongQuestionMapper.updateById(existing);
        } else {
            // 新增错题
            WrongQuestion wq = new WrongQuestion();
            wq.setUserId(userId);
            wq.setQuestionId(questionId);
            wq.setSourceType(sourceType);
            wq.setSourceId(sourceId);
            wq.setWrongCount(1);
            wrongQuestionMapper.insert(wq);
        }
    }
    
    /**
     * 删除错题（做对后删除）
     */
    @Transactional
    public void removeWrongQuestion(Long userId, Long questionId) {
        wrongQuestionMapper.delete(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getUserId, userId)
                        .eq(WrongQuestion::getQuestionId, questionId));
    }
    
    /**
     * 获取用户错题列表
     */
    public IPage<WrongQuestion> getUserWrongQuestions(Long userId, int page, int size) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
                .orderByDesc(WrongQuestion::getUpdatedAt);
        
        IPage<WrongQuestion> result = wrongQuestionMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 填充题目信息
        result.getRecords().forEach(wq -> {
            Question question = questionMapper.selectById(wq.getQuestionId());
            if (question != null) {
                if (question.getType().equals(Constants.QUESTION_SINGLE) ||
                        question.getType().equals(Constants.QUESTION_MULTIPLE)) {
                    question.setOptions(questionOptionMapper.selectByQuestionId(question.getId()));
                }
                wq.setQuestion(question);
            }
        });
        
        return result;
    }
    
    /**
     * 获取错题练习题目
     */
    public List<Question> getWrongPracticeQuestions(Long userId, Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }
        
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
                .orderByDesc(WrongQuestion::getWrongCount)
                .last("LIMIT " + count);
        
        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);
        
        List<Long> questionIds = wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .collect(Collectors.toList());
        
        if (questionIds.isEmpty()) {
            return List.of();
        }
        
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        
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
     * 提交错题练习答案
     */
    @Transactional
    public int submitWrongPractice(Long userId, List<AnswerDTO> answers) {
        int correctCount = 0;
        
        for (AnswerDTO answer : answers) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;
            
            boolean isCorrect = checkAnswer(question, answer.getAnswer());
            
            if (isCorrect) {
                correctCount++;
                // 做对了，从错题本删除
                removeWrongQuestion(userId, answer.getQuestionId());
            } else {
                // 做错了，增加错误次数
                addWrongQuestion(userId, answer.getQuestionId(), 
                        Constants.WRONG_SOURCE_PRACTICE, null);
            }
        }
        
        return correctCount;
    }
    
    /**
     * 获取用户错题数量
     */
    public Long getUserWrongQuestionCount(Long userId) {
        return wrongQuestionMapper.selectCount(
                new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getUserId, userId));
    }
    
    /**
     * 清空用户错题本
     */
    @Transactional
    public void clearWrongQuestions(Long userId) {
        wrongQuestionMapper.delete(
                new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getUserId, userId));
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
                String[] correctOptions = correctAnswer.split(",");
                String[] userOptions = userAnswer.split(",");
                Arrays.sort(correctOptions);
                Arrays.sort(userOptions);
                return Arrays.equals(correctOptions, userOptions);
                
            case Constants.QUESTION_FILL:
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
                
            default:
                return false;
        }
    }
}
