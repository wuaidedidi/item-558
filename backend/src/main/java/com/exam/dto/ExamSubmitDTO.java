package com.exam.dto;

import lombok.Data;

import java.util.List;

/**
 * 考试提交DTO
 */
@Data
public class ExamSubmitDTO {
    
    private Long examId;
    
    private Long recordId;
    
    private List<AnswerDTO> answers;
}
