package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.Category;
import com.exam.exception.BusinessException;
import com.exam.mapper.CategoryMapper;
import com.exam.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务
 */
@Service
public class CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    /**
     * 获取分类列表（分页）
     */
    public IPage<Category> getCategoryList(int page, int size, String keyword) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Category::getName, keyword);
        }
        wrapper.orderByAsc(Category::getSortOrder).orderByDesc(Category::getCreatedAt);
        
        IPage<Category> result = categoryMapper.selectPage(new Page<>(page, size), wrapper);
        // 填充题目数量
        result.getRecords().forEach(c -> {
            c.setQuestionCount(questionMapper.countByCategoryId(c.getId()));
        });
        return result;
    }
    
    /**
     * 获取所有启用的分类
     */
    public List<Category> getAllEnabledCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder);
        
        List<Category> categories = categoryMapper.selectList(wrapper);
        categories.forEach(c -> {
            c.setQuestionCount(questionMapper.countByCategoryId(c.getId()));
        });
        return categories;
    }
    
    /**
     * 根据ID获取分类
     */
    public Category getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category != null) {
            category.setQuestionCount(questionMapper.countByCategoryId(id));
        }
        return category;
    }
    
    /**
     * 创建分类
     */
    @Transactional
    public void createCategory(Category category) {
        // 检查名称是否重复
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName()));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        categoryMapper.insert(category);
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public void updateCategory(Long id, Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查名称是否与其他分类重复
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getName, category.getName())
                        .ne(Category::getId, id));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        
        category.setId(id);
        categoryMapper.updateById(category);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查是否有关联题目
        Integer questionCount = questionMapper.countByCategoryId(id);
        if (questionCount > 0) {
            throw new BusinessException("该分类下有 " + questionCount + " 道题目，无法删除");
        }
        
        categoryMapper.deleteById(id);
    }
}
