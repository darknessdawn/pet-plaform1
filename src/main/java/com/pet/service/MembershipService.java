package com.pet.service;

import com.pet.vo.MembershipLevelVO;
import com.pet.vo.UserMembershipVO;

import java.util.List;

public interface MembershipService {

    /**
     * 获取会员等级列表
     */
    List<MembershipLevelVO> getMembershipLevels();

    /**
     * 获取我的会员信息
     */
    UserMembershipVO getMyMembership(Long userId);

    /**
     * 升级会员
     */
    boolean upgradeMembership(Long userId, Long levelId);

    /**
     * 添加成长值
     */
    boolean addGrowthValue(Long userId, Integer value);

    /**
     * 根据消费金额计算应得积分
     */
    Integer calculatePoints(Long userId, Double amount);

    /**
     * 添加积分
     */
    boolean addPoints(Long userId, Integer points, String source, Long relatedId, String description);

    /**
     * 消费积分
     */
    boolean consumePoints(Long userId, Integer points, String source, Long relatedId, String description);
}
