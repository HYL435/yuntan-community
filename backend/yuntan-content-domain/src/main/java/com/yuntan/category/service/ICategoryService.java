package com.yuntan.category.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.category.dto.CategoryDTO;
import com.yuntan.category.dto.CategoryStatusDTO;
import com.yuntan.category.dto.CategoryUpdateDTO;
import com.yuntan.category.entity.Category;
import com.yuntan.category.vo.CategoryContentVO;
import com.yuntan.category.vo.CategoryFrontVO;
import com.yuntan.category.vo.CategoryVO;

import java.util.List;

public interface ICategoryService extends IService<Category> {

    /**
     * 获取所有分类名称
     */
    List<CategoryFrontVO> getAllCategoryNames();

    /**
     * 获取所有分类管理端
     */
    List<CategoryVO> getAdminCategory();

    /**
     * 添加分类
     */
    void addCategory(CategoryDTO categoryDTO);

    /**
     * 修改分类
     */
    void updateCategory(CategoryUpdateDTO categoryUpdateDTO);

    /**
     * 根据id获取分类内容
     */
    CategoryContentVO getCategoryById(Long id);

    /**
     * 修改分类状态
     */
    void changeCategoryStatus(CategoryStatusDTO categoryStatusDTO);

    /**
     * 删除分类
     */
    void removeCategoryById(Long id);
}
