package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserMembershipVO {

    private Long id;

    private Long userId;

    private Long levelId;

    private String levelName;

    private Integer levelCode;

    private Integer points; // 当前积分

    private Integer totalPoints; // 累计积分

    private Integer usedPoints; // 已消耗积分

    private Integer growthValue; // 成长值

    private BigDecimal discountRate; // 当前等级折扣率

    private BigDecimal pointsRate; // 当前等级积分倍率

    private LocalDateTime expireDate; // 会员到期时间
}
