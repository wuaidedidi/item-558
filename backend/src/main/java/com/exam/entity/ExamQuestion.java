package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 试卷题目关联实体
 */
@Data
@TableName("exam_questions")
public class ExamQuestion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long examId;
    
    private Long questionId;
    
    private Integer score;
    
    private Integer sortOrder;
    
    @TableField(exist = false)
    private Question question;
}
