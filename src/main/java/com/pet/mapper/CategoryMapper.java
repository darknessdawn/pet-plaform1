package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND status = 1 AND deleted = 0 ORDER BY sort")
    List<Category> selectByParentId(Long parentId);
    
    @Select("SELECT * FROM category WHERE level = #{level} AND status = 1 AND deleted = 0 ORDER BY sort")
    List<Category> selectByLevel(Integer level);
}
