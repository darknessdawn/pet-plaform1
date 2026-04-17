package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_membership")
public class UserMembership extends BaseEntity {

    private Long userId;

    private Long levelId; // 会员等级 ID

    private Integer points; // 当前积分

    private Integer totalPoints; // 累计积分

    private Integer usedPoints; // 已消耗积分

    private Integer growthValue; // 成长值

    private LocalDateTime expireDate; // 会员到期时间

    private Integer status;
}
