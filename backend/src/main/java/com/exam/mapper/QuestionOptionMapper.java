package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目选项Mapper
 */
@Mapper
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {
    
    default List<QuestionOption> selectByQuestionId(Long questionId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuestionOption>()
                .eq(QuestionOption::getQuestionId, questionId)
                .orderByAsc(QuestionOption::getSortOrder));
    }
    
    default void deleteByQuestionId(Long questionId) {
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuestionOption>()
                .eq(QuestionOption::getQuestionId, questionId));
    }
}
