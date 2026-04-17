package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PointsProductVO {

    private Long id;

    private String name;

    private String description;

    private Integer pointsPrice; // 积分价格

    private BigDecimal cashPrice; // 现金价格

    private Integer stock;

    private String image;

    private Integer status;
}
