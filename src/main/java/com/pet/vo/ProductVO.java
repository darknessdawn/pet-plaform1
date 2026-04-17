package com.pet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductVO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private BigDecimal originalPrice;
    
    private Integer stock;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Long sellerId;
    
    private String sellerName;
    
    private String mainImage;
    
    private List<String> images;
    
    private Integer sales;
    
    private Integer status;
    
    private Integer reviewCount;
    
    private Double rating;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
