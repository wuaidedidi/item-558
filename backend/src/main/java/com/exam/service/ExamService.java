package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.ExamDTO;
import com.exam.dto.ExamQuestionDTO;
import com.exam.entity.Exam;
import com.exam.entity.ExamQuestion;
import com.exam.entity.Question;
import com.exam.exception.BusinessException;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.ExamQuestionMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 试卷服务
 */
@Service
public class ExamService {
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionOptionMapper questionOptionMapper;
    
    /**
     * 获取试卷列表（分页）
     */
    public IPage<Exam> getExamList(int page, int size, Integer status, String keyword) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Exam::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Exam::getTitle, keyword);
        }
        wrapper.orderByDesc(Exam::getCreatedAt);
        
        IPage<Exam> result = examMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 填充题目数量
        result.getRecords().forEach(exam -> {
            Long count = examQuestionMapper.selectCount(
                    new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, exam.getId()));
            exam.setQuestionCount(count.intValue());
        });
        
        return result;
    }
    
    /**
     * 获取已发布的试卷列表
     */
    public List<Exam> getPublishedExams() {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exam::getStatus, Constants.EXAM_PUBLISHED)
                .orderByDesc(Exam::getCreatedAt);
        
        List<Exam> exams = examMapper.selectList(wrapper);
        exams.forEach(exam -> {
            Long count = examQuestionMapper.selectCount(
                    new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, exam.getId()));
            exam.setQuestionCount(count.intValue());
        });
        return exams;
    }
    
    /**
     * 根据ID获取试卷
     */
    public Exam getExamById(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam != null) {
            fillExamDetails(exam);
        }
        return exam;
    }
    
    /**
     * 获取试卷详情（包含题目）
     */
    public Exam getExamWithQuestions(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        
        fillExamDetails(exam);
        return exam;
    }
    
    /**
     * 创建试卷
     */
    @Transactional
    public void createExam(ExamDTO dto) {
        Exam exam = new Exam();
        BeanUtils.copyProperties(dto, exam);
        
        // 计算总分
        int totalScore = 0;
        if (dto.getQuestions() != null) {
            totalScore = dto.getQuestions().stream()
                    .mapToInt(q -> q.getScore() != null ? q.getScore() : 0)
                    .sum();
        }
        exam.setTotalScore(totalScore);
        
        examMapper.insert(exam);
        
        // 保存试卷题目
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            saveExamQuestions(exam.getId(), dto.getQuestions());
        }
    }
    
    /**
     * 更新试卷
     */
    @Transactional
    public void updateExam(Long id, ExamDTO dto) {
        Exam existing = examMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("试卷不存在");
        }
        
        if (existing.getStatus() == Constants.EXAM_PUBLISHED) {
            throw new BusinessException("已发布的试卷无法修改");
        }
        
        Exam exam = new Exam();
        BeanUtils.copyProperties(dto, exam);
        exam.setId(id);
        
        // 计算总分
        int totalScore = 0;
        if (dto.getQuestions() != null) {
            totalScore = dto.getQuestions().stream()
                    .mapToInt(q -> q.getScore() != null ? q.getScore() : 0)
                    .sum();
        }
        exam.setTotalScore(totalScore);
        
        examMapper.updateById(exam);
        
        // 更新试卷题目
        examQuestionMapper.delete(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, id));
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            saveExamQuestions(id, dto.getQuestions());
        }
    }
    
    /**
     * 发布试卷
     */
    @Transactional
    public void publishExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查是否有题目
        Long count = examQuestionMapper.selectCount(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, id));
        if (count == 0) {
            throw new BusinessException("试卷没有添加题目，无法发布");
        }
        
        exam.setStatus(Constants.EXAM_PUBLISHED);
        examMapper.updateById(exam);
    }
    
    /**
     * 结束试卷
     */
    @Transactional
    public void endExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        
        exam.setStatus(Constants.EXAM_ENDED);
        examMapper.updateById(exam);
    }
    
    /**
     * 删除试卷
     */
    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        
        if (exam.getStatus() == Constants.EXAM_PUBLISHED) {
            throw new BusinessException("已发布的试卷无法删除");
        }
        
        // 删除试卷题目
        examQuestionMapper.delete(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, id));
        // 删除试卷
        examMapper.deleteById(id);
    }
    
    /**
     * 填充试卷详情
     */
    private void fillExamDetails(Exam exam) {
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId, exam.getId())
                        .orderByAsc(ExamQuestion::getSortOrder));
        
        // 填充题目详情（包含选项）
        examQuestions.forEach(eq -> {
            Question question = questionMapper.selectById(eq.getQuestionId());
            if (question != null) {
                // 加载题目选项
                question.setOptions(questionOptionMapper.selectByQuestionId(question.getId()));
            }
            eq.setQuestion(question);
        });
        
        exam.setQuestions(examQuestions);
        exam.setQuestionCount(examQuestions.size());
    }
    
    /**
     * 保存试卷题目
     */
    private void saveExamQuestions(Long examId, List<ExamQuestionDTO> questions) {
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestionDTO dto = questions.get(i);
            ExamQuestion eq = new ExamQuestion();
            eq.setExamId(examId);
            eq.setQuestionId(dto.getQuestionId());
            eq.setScore(dto.getScore() != null ? dto.getScore() : 10);
            eq.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : i);
            examQuestionMapper.insert(eq);
        }
    }
}
