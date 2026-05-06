package com.exam.dto;

import lombok.Data;

/**
 * 题目选项DTO
 */
@Data
public class QuestionOptionDTO {
    
    private Long id;
    
    private String optionLabel;
    
    private String optionContent;
    
    private Boolean isCorrect;
    
    private Integer sortOrder;
}
