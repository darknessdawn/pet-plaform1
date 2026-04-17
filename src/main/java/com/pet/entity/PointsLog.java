package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_log")
public class PointsLog extends BaseEntity {

    private Long userId;

    private Integer points; // 积分变动数量

    private Integer type; // 1-获取，2-消费，3-兑换

    private String source; // 来源

    private Long relatedId; // 关联 ID

    private String description; // 描述
}
