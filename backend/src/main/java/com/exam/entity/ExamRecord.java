package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试记录实体
 */
@Data
@TableName("exam_records")
public class ExamRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long examId;
    
    private Integer score;
    
    /**
     * 状态: ONGOING-进行中 SUBMITTED-已提交 GRADED-已评分
     */
    private String status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @TableField(exist = false)
    private List<ExamAnswer> answers;
    
    @TableField(exist = false)
    private Exam exam;
    
    @TableField(exist = false)
    private String username;
}
