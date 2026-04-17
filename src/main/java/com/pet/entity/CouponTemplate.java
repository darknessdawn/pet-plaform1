package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_template")
public class CouponTemplate extends BaseEntity {

    private String name;

    private Integer type; // 1-满减，2-折扣

    private BigDecimal discountAmount; // 减免金额

    private BigDecimal discountRate; // 折扣率

    private BigDecimal minPurchase; // 最低消费金额

    private BigDecimal maxDiscount; // 最大优惠金额

    private Integer totalCount; // 发放总数

    private Integer issuedCount; // 已发放数量

    private Integer perUserLimit; // 每人限领

    private Integer status; // 1-可用，0-停用

    private LocalDateTime validStart; // 有效开始时间

    private LocalDateTime validEnd; // 有效结束时间
}
