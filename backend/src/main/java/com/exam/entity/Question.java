package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体
 */
@Data
@TableName("questions")
public class Question {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long categoryId;
    
    /**
     * 题目类型: SINGLE/MULTIPLE/JUDGE/ESSAY/FILL
     */
    private String type;
    
    private String content;
    
    /**
     * 正确答案
     * 单选: A/B/C/D
     * 多选: A,B,C
     * 判断: TRUE/FALSE
     * 问答: 答案文本
     * 填空: 答案1,答案2,...
     */
    private String answer;
    
    private String analysis;
    
    /**
     * 难度: 1-简单 2-中等 3-困难
     */
    private Integer difficulty;
    
    private Integer score;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private List<QuestionOption> options;
    
    @TableField(exist = false)
    private String categoryName;
}
