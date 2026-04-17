package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_behavior")
public class UserBehavior extends BaseEntity {
    
    private Long userId;
    
    private Long productId;
    
    private Integer behaviorType;
    
    private Double score;
    
    private String remark;
}
