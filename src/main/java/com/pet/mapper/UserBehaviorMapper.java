package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {
    
    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND deleted = 0")
    List<UserBehavior> selectByUserId(Long userId);
    
    @Select("SELECT * FROM user_behavior WHERE product_id = #{productId} AND deleted = 0")
    List<UserBehavior> selectByProductId(Long productId);
    
    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND product_id = #{productId} AND behavior_type = #{behaviorType} AND deleted = 0")
    UserBehavior selectByUserProductAndType(@Param("userId") Long userId, @Param("productId") Long productId, @Param("behaviorType") Integer behaviorType);
    
    @Select("SELECT DISTINCT user_id FROM user_behavior WHERE deleted = 0")
    List<Long> selectAllUserIds();
    
    @Select("SELECT DISTINCT product_id FROM user_behavior WHERE deleted = 0")
    List<Long> selectAllProductIds();
}
