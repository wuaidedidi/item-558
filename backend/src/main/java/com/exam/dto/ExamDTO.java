package com.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷DTO
 */
@Data
public class ExamDTO {
    
    private Long id;
    
    @NotBlank(message = "试卷标题不能为空")
    private String title;
    
    private String description;
    
    private Integer totalScore;
    
    @NotNull(message = "及格分数不能为空")
    private Integer passScore;
    
    @NotNull(message = "考试时长不能为空")
    private Integer duration;
    
    private Integer status = 0;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    /**
     * 试卷题目列表
     */
    private List<ExamQuestionDTO> questions;
}
