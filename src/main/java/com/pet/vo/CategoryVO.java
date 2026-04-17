package com.pet.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {
    
    private Long id;
    
    private String name;
    
    private Long parentId;
    
    private Integer level;
    
    private String icon;
    
    private Integer sort;
    
    private List<CategoryVO> children;
}
