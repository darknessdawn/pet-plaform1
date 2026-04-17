package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartVO {
    
    private Long id;
    
    private Long userId;
    
    private Long productId;
    
    private String productName;
    
    private String productImage;
    
    private BigDecimal productPrice;
    
    private Integer quantity;
    
    private Integer stock;
    
    private Integer selected;
    
    private BigDecimal totalPrice;
}
