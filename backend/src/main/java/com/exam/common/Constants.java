package com.exam.common;

/**
 * 系统常量
 */
public class Constants {
    
    // 用户角色
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    
    // 用户状态
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;
    
    // 题目类型
    public static final String QUESTION_SINGLE = "SINGLE";      // 单选题
    public static final String QUESTION_MULTIPLE = "MULTIPLE";  // 多选题
    public static final String QUESTION_JUDGE = "JUDGE";        // 判断题
    public static final String QUESTION_ESSAY = "ESSAY";        // 问答题
    public static final String QUESTION_FILL = "FILL";          // 填空题
    
    // 题目难度
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_HARD = 3;
    
    // 考试状态
    public static final int EXAM_DRAFT = 0;      // 草稿
    public static final int EXAM_PUBLISHED = 1;  // 已发布
    public static final int EXAM_ENDED = 2;      // 已结束
    
    // 考试记录状态
    public static final String RECORD_ONGOING = "ONGOING";      // 进行中
    public static final String RECORD_SUBMITTED = "SUBMITTED";  // 已提交
    public static final String RECORD_GRADED = "GRADED";        // 已评分
    
    // 错题来源
    public static final String WRONG_SOURCE_PRACTICE = "PRACTICE";  // 练习
    public static final String WRONG_SOURCE_EXAM = "EXAM";          // 考试
    
    // 练习模式
    public static final String PRACTICE_MODE_NORMAL = "NORMAL";     // 普通模式
    public static final String PRACTICE_MODE_WRONG = "WRONG";       // 错题模式
}
