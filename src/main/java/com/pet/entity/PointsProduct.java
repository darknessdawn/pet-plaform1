package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_product")
public class PointsProduct extends BaseEntity {

    private String name;

    private String description;

    private Integer pointsPrice; // 积分价格

    private BigDecimal cashPrice; // 现金价格

    private Integer stock;

    private String image;

    private Integer status;
}
