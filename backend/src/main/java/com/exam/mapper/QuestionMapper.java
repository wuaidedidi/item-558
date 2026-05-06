package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    
    @Select("SELECT q.*, c.name as category_name FROM questions q " +
            "LEFT JOIN categories c ON q.category_id = c.id WHERE q.id = #{id}")
    Question selectWithCategory(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM questions WHERE category_id = #{categoryId}")
    Integer countByCategoryId(@Param("categoryId") Long categoryId);
    
    @Select("SELECT q.*, c.name as category_name FROM questions q " +
            "LEFT JOIN categories c ON q.category_id = c.id " +
            "WHERE q.category_id = #{categoryId} AND q.status = 1 ORDER BY RANDOM() LIMIT #{limit}")
    List<Question> selectRandomByCategory(@Param("categoryId") Long categoryId, @Param("limit") Integer limit);
}
