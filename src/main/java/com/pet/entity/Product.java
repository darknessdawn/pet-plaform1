package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private BigDecimal originalPrice;
    
    private Integer stock;
    
    @Version
    private Integer version;
    
    private Long categoryId;
    
    private Long sellerId;
    
    private String mainImage;
    
    private String images;
    
    private Integer sales;
    
    private Integer status;
    
    private Integer reviewCount;
    
    private Double rating;
}
