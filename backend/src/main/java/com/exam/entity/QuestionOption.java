package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 题目选项实体（用于单选题和多选题）
 */
@Data
@TableName("question_options")
public class QuestionOption {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long questionId;
    
    /**
     * 选项标签: A/B/C/D...
     */
    private String optionLabel;
    
    private String optionContent;
    
    private Boolean isCorrect;
    
    private Integer sortOrder;
}
