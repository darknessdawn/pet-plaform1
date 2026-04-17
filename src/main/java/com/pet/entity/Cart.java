package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity {
    
    private Long userId;
    
    private Long productId;
    
    private Integer quantity;
    
    private Integer selected;
}
