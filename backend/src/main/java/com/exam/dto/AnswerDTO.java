package com.exam.dto;

import lombok.Data;

/**
 * 答案DTO
 */
@Data
public class AnswerDTO {
    
    private Long questionId;
    
    private String answer;
}
