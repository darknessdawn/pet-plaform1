package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_order")
public class PointsOrder extends BaseEntity {

    private String orderNo;

    private Long userId;

    private Long productId;

    private Integer pointsUsed; // 使用的积分

    private BigDecimal cashPaid; // 支付的现金

    private Integer status; // 0-待兑换，1-已完成

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;
}
