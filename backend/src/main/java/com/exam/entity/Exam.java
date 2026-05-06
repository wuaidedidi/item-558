package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷实体
 */
@Data
@TableName("exams")
public class Exam {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String description;
    
    private Integer totalScore;
    
    private Integer passScore;
    
    /**
     * 考试时长（分钟）
     */
    private Integer duration;
    
    /**
     * 状态: 0-草稿 1-已发布 2-已结束
     */
    private Integer status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private List<ExamQuestion> questions;
    
    @TableField(exist = false)
    private Integer questionCount;
}
