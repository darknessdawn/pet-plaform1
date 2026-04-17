package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCouponVO {

    private Long id;

    private Long userId;

    private Long couponTemplateId;

    private String couponName;

    private Integer type; // 1-满减，2-折扣

    private BigDecimal discountAmount; // 减免金额

    private BigDecimal discountRate; // 折扣率

    private BigDecimal minPurchase; // 最低消费金额

    private BigDecimal maxDiscount; // 最大优惠金额

    private Integer status; // 0-未使用，1-已使用，2-已过期

    private LocalDateTime gettime; // 领取时间

    private LocalDateTime useTime; // 使用时间

    private LocalDateTime validStart; // 有效开始时间

    private LocalDateTime validEnd; // 有效结束时间
}
