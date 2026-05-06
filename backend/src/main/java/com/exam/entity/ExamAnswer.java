package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 考试答案实体
 */
@Data
@TableName("exam_answers")
public class ExamAnswer {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long recordId;
    
    private Long questionId;
    
    private String userAnswer;
    
    private Boolean isCorrect;
    
    private Integer score;
    
    @TableField(exist = false)
    private Question question;
}
