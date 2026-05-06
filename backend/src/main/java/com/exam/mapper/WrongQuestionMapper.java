package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.WrongQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 错题本Mapper
 */
@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {
    
    @Select("SELECT wq.*, q.content, q.type, q.category_id, q.answer, q.analysis, q.difficulty, q.score " +
            "FROM wrong_questions wq " +
            "JOIN questions q ON wq.question_id = q.id " +
            "WHERE wq.user_id = #{userId} ORDER BY wq.updated_at DESC")
    List<WrongQuestion> selectWithQuestionByUserId(@Param("userId") Long userId);
}
