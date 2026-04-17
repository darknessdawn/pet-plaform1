package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Select("SELECT * FROM product WHERE category_id = #{categoryId} AND status = 1 AND deleted = 0 ORDER BY sales DESC")
    List<Product> selectByCategoryId(Long categoryId);
    
    @Select("SELECT * FROM product WHERE status = 1 AND deleted = 0 ORDER BY sales DESC LIMIT #{limit}")
    List<Product> selectHotProducts(Integer limit);
    
    @Select("SELECT * FROM product WHERE status = 1 AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Product> selectNewProducts(Integer limit);
    
    @Update("UPDATE product SET stock = stock - #{quantity}, sales = sales + #{quantity}, version = version + 1 " +
            "WHERE id = #{productId} AND stock >= #{quantity} AND version = #{version}")
    int reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity, @Param("version") Integer version);
    
    @Select("SELECT * FROM product WHERE seller_id = #{sellerId} AND deleted = 0 ORDER BY create_time DESC")
    List<Product> selectBySellerId(Long sellerId);
}
