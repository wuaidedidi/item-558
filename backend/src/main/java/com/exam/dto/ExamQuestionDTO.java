package com.exam.dto;

import lombok.Data;

/**
 * 试卷题目DTO
 */
@Data
public class ExamQuestionDTO {
    
    private Long id;
    
    private Long questionId;
    
    private Integer score;
    
    private Integer sortOrder;
}
