package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review")
public class Review extends BaseEntity {
    
    private Long userId;
    
    private Long productId;
    
    private Long orderId;
    
    private Integer rating;
    
    private String content;
    
    private String images;
    
    private Integer status;
}
