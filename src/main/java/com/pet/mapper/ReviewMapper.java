package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    @Select("SELECT * FROM review WHERE product_id = #{productId} AND status = 1 AND deleted = 0 ORDER BY create_time DESC")
    List<Review> selectByProductId(Long productId);
    
    @Select("SELECT * FROM review WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Review> selectByUserId(Long userId);
    
    @Select("SELECT * FROM review WHERE order_id = #{orderId} AND deleted = 0")
    Review selectByOrderId(Long orderId);
    
    @Select("SELECT AVG(rating) FROM review WHERE product_id = #{productId} AND status = 1 AND deleted = 0")
    Double selectAverageRatingByProductId(Long productId);
    
    @Select("SELECT COUNT(*) FROM review WHERE product_id = #{productId} AND status = 1 AND deleted = 0")
    Integer countByProductId(Long productId);
}
