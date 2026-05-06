package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.QuestionDTO;
import com.exam.dto.QuestionOptionDTO;
import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import com.exam.exception.BusinessException;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目服务
 */
@Service
public class QuestionService {
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionOptionMapper questionOptionMapper;
    
    /**
     * 获取题目列表（分页）
     */
    public IPage<Question> getQuestionList(int page, int size, Long categoryId, String type, Integer difficulty, String keyword) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(Question::getCategoryId, categoryId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Question::getType, type);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Question::getContent, keyword);
        }
        wrapper.orderByDesc(Question::getCreatedAt);
        
        IPage<Question> result = questionMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 填充选项和分类名称
        result.getRecords().forEach(this::fillQuestionDetails);
        
        return result;
    }
    
    /**
     * 获取所有启用的题目
     */
    public List<Question> getAllEnabledQuestions(Long categoryId, String type) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getStatus, 1);
        
        if (categoryId != null) {
            wrapper.eq(Question::getCategoryId, categoryId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Question::getType, type);
        }
        wrapper.orderByDesc(Question::getCreatedAt);
        
        List<Question> questions = questionMapper.selectList(wrapper);
        questions.forEach(this::fillQuestionDetails);
        return questions;
    }
    
    /**
     * 根据ID获取题目
     */
    public Question getQuestionById(Long id) {
        Question question = questionMapper.selectWithCategory(id);
        if (question != null) {
            fillQuestionDetails(question);
        }
        return question;
    }
    
    /**
     * 创建题目
     */
    @Transactional
    public void createQuestion(QuestionDTO dto) {
        Question question = new Question();
        BeanUtils.copyProperties(dto, question);
        questionMapper.insert(question);
        
        // 保存选项（仅适用于选择题）
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            saveOptions(question.getId(), dto.getOptions());
        }
    }
    
    /**
     * 更新题目
     */
    @Transactional
    public void updateQuestion(Long id, QuestionDTO dto) {
        Question existing = questionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("题目不存在");
        }
        
        Question question = new Question();
        BeanUtils.copyProperties(dto, question);
        question.setId(id);
        questionMapper.updateById(question);
        
        // 更新选项
        if (dto.getType().equals(Constants.QUESTION_SINGLE) || 
                dto.getType().equals(Constants.QUESTION_MULTIPLE)) {
            // 删除旧选项
            questionOptionMapper.deleteByQuestionId(id);
            // 保存新选项
            if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                saveOptions(id, dto.getOptions());
            }
        }
    }
    
    /**
     * 删除题目
     */
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        
        // 删除选项
        questionOptionMapper.deleteByQuestionId(id);
        // 删除题目
        questionMapper.deleteById(id);
    }
    
    /**
     * 批量删除题目
     */
    @Transactional
    public void batchDeleteQuestions(List<Long> ids) {
        ids.forEach(this::deleteQuestion);
    }
    
    /**
     * 按分类随机获取题目
     */
    public List<Question> getRandomQuestionsByCategory(Long categoryId, Integer count) {
        List<Question> questions = questionMapper.selectRandomByCategory(categoryId, count);
        questions.forEach(this::fillQuestionDetails);
        return questions;
    }
    
    /**
     * 填充题目详情
     */
    private void fillQuestionDetails(Question question) {
        // 填充选项
        if (question.getType().equals(Constants.QUESTION_SINGLE) || 
                question.getType().equals(Constants.QUESTION_MULTIPLE)) {
            question.setOptions(questionOptionMapper.selectByQuestionId(question.getId()));
        }
    }
    
    /**
     * 保存选项
     */
    private void saveOptions(Long questionId, List<QuestionOptionDTO> options) {
        for (int i = 0; i < options.size(); i++) {
            QuestionOptionDTO dto = options.get(i);
            QuestionOption option = new QuestionOption();
            option.setQuestionId(questionId);
            option.setOptionLabel(dto.getOptionLabel());
            option.setOptionContent(dto.getOptionContent());
            option.setIsCorrect(dto.getIsCorrect() != null ? dto.getIsCorrect() : false);
            option.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : i);
            questionOptionMapper.insert(option);
        }
    }
    
    /**
     * 根据ID列表获取题目
     */
    public List<Question> getQuestionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Question> questions = questionMapper.selectBatchIds(ids);
        questions.forEach(this::fillQuestionDetails);
        return questions;
    }
}
