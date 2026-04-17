package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
public class UserCoupon extends BaseEntity {

    private Long userId;

    private Long couponTemplateId;

    private Integer status; // 0-未使用，1-已使用，2-已过期

    private Long orderId; // 使用的订单 ID

    private LocalDateTime getTime; // 领取时间

    private LocalDateTime useTime; // 使用时间

    private LocalDateTime validStart; // 有效开始时间

    private LocalDateTime validEnd; // 有效结束时间
}
