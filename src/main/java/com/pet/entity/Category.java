package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {
    
    private String name;
    
    private Long parentId;
    
    private Integer level;
    
    private String icon;
    
    private Integer sort;
    
    private Integer status;
}
