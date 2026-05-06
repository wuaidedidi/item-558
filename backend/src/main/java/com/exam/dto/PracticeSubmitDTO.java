package com.exam.dto;

import lombok.Data;

import java.util.List;

/**
 * 练习提交DTO
 */
@Data
public class PracticeSubmitDTO {
    
    private Long categoryId;
    
    private String mode;
    
    private List<AnswerDTO> answers;
}
