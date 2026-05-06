package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本实体
 */
@Data
@TableName("wrong_questions")
public class WrongQuestion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long questionId;
    
    /**
     * 来源类型: PRACTICE-练习 EXAM-考试
     */
    private String sourceType;
    
    private Long sourceId;
    
    private Integer wrongCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private Question question;
}
