package com.pet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    
    @NotNull(message = "地址 ID 不能为空")
    private Long addressId;
        
    private String remark;
        
    private List<Long> cartIds; // 从购物车创建订单时传入
        
    private Long couponId; // 使用的优惠券 ID
}
