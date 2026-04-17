package com.pet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {

    private String name;

    private Integer type; // 1-满减，2-折扣

    private BigDecimal discountAmount; // 减免金额

    private BigDecimal discountRate; // 折扣率

    private BigDecimal minPurchase; // 最低消费金额

    private BigDecimal maxDiscount; // 最大优惠金额

    private Integer totalCount; // 发放总数

    private Integer perUserLimit; // 每人限领

    private LocalDateTime validStart; // 有效开始时间

    private LocalDateTime validEnd; // 有效结束时间
}
