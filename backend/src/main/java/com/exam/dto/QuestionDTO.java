package com.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 题目DTO
 */
@Data
public class QuestionDTO {
    
    private Long id;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    @NotBlank(message = "题目类型不能为空")
    private String type;
    
    @NotBlank(message = "题目内容不能为空")
    private String content;
    
    @NotBlank(message = "答案不能为空")
    private String answer;
    
    private String analysis;
    
    private Integer difficulty = 1;
    
    private Integer score = 10;
    
    private Integer status = 1;
    
    /**
     * 选项列表（用于单选题和多选题）
     */
    private List<QuestionOptionDTO> options;
}
