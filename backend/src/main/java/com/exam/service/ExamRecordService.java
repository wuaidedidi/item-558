package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.AnswerDTO;
import com.exam.dto.ExamSubmitDTO;
import com.exam.entity.*;
import com.exam.exception.BusinessException;
import com.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 考试记录服务
 */
@Service
public class ExamRecordService {
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private ExamAnswerMapper examAnswerMapper;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionOptionMapper questionOptionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    /**
     * 开始考试
     */
    @Transactional
    public ExamRecord startExam(Long userId, Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        
        if (exam.getStatus() != Constants.EXAM_PUBLISHED) {
            throw new BusinessException("该试卷尚未发布");
        }
        
        // 检查时间限制
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束");
        }
        
        // 检查是否已有进行中的考试
        ExamRecord existingRecord = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStatus, Constants.RECORD_ONGOING));
        
        if (existingRecord != null) {
            // 返回已有记录
            return existingRecord;
        }
        
        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(examId);
        record.setStatus(Constants.RECORD_ONGOING);
        record.setStartTime(LocalDateTime.now());
        record.setScore(0);
        
        examRecordMapper.insert(record);
        
        return record;
    }
    
    /**
     * 提交考试
     */
    @Transactional
    public ExamRecord submitExam(Long userId, ExamSubmitDTO dto) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此考试记录");
        }
        
        if (!Constants.RECORD_ONGOING.equals(record.getStatus())) {
            throw new BusinessException("该考试已提交");
        }
        
        Exam exam = examMapper.selectById(record.getExamId());
        
        // 获取试卷题目及分值
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, exam.getId()));
        
        int totalScore = 0;
        
        // 保存答案并判分
        for (AnswerDTO answer : dto.getAnswers()) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;
            
            // 获取该题在试卷中的分值
            int questionScore = examQuestions.stream()
                    .filter(eq -> eq.getQuestionId().equals(answer.getQuestionId()))
                    .findFirst()
                    .map(ExamQuestion::getScore)
                    .orElse(question.getScore());
            
            boolean isCorrect = checkAnswer(question, answer.getAnswer());
            int score = isCorrect ? questionScore : 0;
            
            ExamAnswer ea = new ExamAnswer();
            ea.setRecordId(record.getId());
            ea.setQuestionId(answer.getQuestionId());
            ea.setUserAnswer(answer.getAnswer());
            ea.setIsCorrect(isCorrect);
            ea.setScore(score);
            examAnswerMapper.insert(ea);
            
            totalScore += score;
            
            // 答错了，添加到错题本
            if (!isCorrect) {
                wrongQuestionService.addWrongQuestion(userId, answer.getQuestionId(),
                        Constants.WRONG_SOURCE_EXAM, record.getId());
            }
        }
        
        // 更新考试记录
        record.setScore(totalScore);
        record.setStatus(Constants.RECORD_GRADED);
        record.setEndTime(LocalDateTime.now());
        examRecordMapper.updateById(record);
        
        return record;
    }
    
    /**
     * 获取用户考试记录
     */
    public IPage<ExamRecord> getUserExamRecords(Long userId, int page, int size) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getStartTime);
        
        IPage<ExamRecord> result = examRecordMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 填充试卷信息
        result.getRecords().forEach(r -> {
            Exam exam = examMapper.selectById(r.getExamId());
            r.setExam(exam);
        });
        
        return result;
    }
    
    /**
     * 获取所有考试记录（管理员）
     */
    public IPage<ExamRecord> getAllExamRecords(int page, int size, Long examId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            wrapper.eq(ExamRecord::getExamId, examId);
        }
        wrapper.orderByDesc(ExamRecord::getStartTime);
        
        IPage<ExamRecord> result = examRecordMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 填充试卷和用户信息
        result.getRecords().forEach(r -> {
            Exam exam = examMapper.selectById(r.getExamId());
            r.setExam(exam);
            User user = userMapper.selectById(r.getUserId());
            if (user != null) {
                r.setUsername(user.getUsername());
            }
        });
        
        return result;
    }
    
    /**
     * 获取考试记录详情
     */
    public ExamRecord getExamRecordDetail(Long recordId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        // 填充试卷信息
        record.setExam(examMapper.selectById(record.getExamId()));
        
        // 填充答案详情
        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId));
        
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
                
            case Constants.QUESTION_ESSAY:
                return false;
                
            default:
                return false;
        }
    }
}
