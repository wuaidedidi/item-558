package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷Mapper
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
}
