package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("membership_level")
public class MembershipLevel extends BaseEntity {

    private String levelName;

    private Integer levelCode; // 1-普通，2-白银，3-黄金，4-钻石

    private Integer minPoints; // 所需最低积分

    private BigDecimal discountRate; // 商品折扣率

    private BigDecimal pointsRate; // 积分倍率

    private Integer freeShipping; // 是否免运费

    private Integer status;
}
