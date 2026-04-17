package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    
    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId} AND deleted = 0")
    List<OrderDetail> selectByOrderId(Long orderId);
}
