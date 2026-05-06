package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习记录实体
 */
@Data
@TableName("practice_records")
public class PracticeRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long categoryId;
    
    /**
     * 练习模式: NORMAL-普通模式 WRONG-错题模式
     */
    private String mode;
    
    private Integer totalCount;
    
    private Integer correctCount;
    
    private Integer score;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(exist = false)
    private List<PracticeAnswer> answers;
    
    @TableField(exist = false)
    private String categoryName;
}
