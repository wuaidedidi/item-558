package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷题目关联Mapper
 */
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
}
