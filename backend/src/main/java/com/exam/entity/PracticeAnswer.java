package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习答案实体
 */
@Data
@TableName("practice_answers")
public class PracticeAnswer {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long recordId;
    
    private Long questionId;
    
    private String userAnswer;
    
    private Boolean isCorrect;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(exist = false)
    private Question question;
}
