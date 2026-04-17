package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.common.BusinessException;
import com.pet.entity.MembershipLevel;
import com.pet.entity.UserMembership;
import com.pet.entity.PointsLog;
import com.pet.mapper.MembershipLevelMapper;
import com.pet.mapper.UserMembershipMapper;
import com.pet.mapper.PointsLogMapper;
import com.pet.service.MembershipService;
import com.pet.vo.MembershipLevelVO;
import com.pet.vo.UserMembershipVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final MembershipLevelMapper membershipLevelMapper;
    private final UserMembershipMapper userMembershipMapper;
    private final PointsLogMapper pointsLogMapper;

    @Override
    public List<MembershipLevelVO> getMembershipLevels() {
        List<MembershipLevel> levels = membershipLevelMapper.selectList(
            new LambdaQueryWrapper<MembershipLevel>()
                .eq(MembershipLevel::getStatus, 1)
                .orderByAsc(MembershipLevel::getMinPoints)
        );
        
        return levels.stream().map(level -> {
            MembershipLevelVO vo = new MembershipLevelVO();
            BeanUtils.copyProperties(level, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public UserMembershipVO getMyMembership(Long userId) {
        UserMembership membership = userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
                .eq(UserMembership::getDeleted, 0)
        );
        
        if (membership == null) {
            // 如果没有会员信息，创建一个默认的普通会员
            membership = createDefaultMembership(userId);
        }
        
        return convertToVO(membership);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean upgradeMembership(Long userId, Long levelId) {
        UserMembership membership = userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
        );
        
        if (membership == null) {
            throw new BusinessException("会员信息不存在");
        }
        
        MembershipLevel newLevel = membershipLevelMapper.selectById(levelId);
        if (newLevel == null) {
            throw new BusinessException("会员等级不存在");
        }
        
        // 检查是否满足升级条件
        if (membership.getGrowthValue() < newLevel.getMinPoints()) {
            throw new BusinessException("成长值不足，无法升级");
        }
        
        membership.setLevelId(levelId);
        userMembershipMapper.updateById(membership);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addGrowthValue(Long userId, Integer value) {
        UserMembership membership = userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
        );
        
        if (membership == null) {
            membership = createDefaultMembership(userId);
        }
        
        membership.setGrowthValue(membership.getGrowthValue() + value);
        userMembershipMapper.updateById(membership);
        
        // 检查是否可以自动升级
        autoUpgradeMembership(membership);
        
        return true;
    }

    @Override
    public Integer calculatePoints(Long userId, Double amount) {
        UserMembership membership = userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
        );
        
        if (membership == null) {
            // 默认普通会员，积分倍率为 1.0
            return (int)(amount * 1.0);
        }
        
        // 加载会员等级信息
        MembershipLevel level = membershipLevelMapper.selectById(membership.getLevelId());
        BigDecimal pointsRate = (level != null && level.getPointsRate() != null) ? 
                                 level.getPointsRate() : BigDecimal.ONE;
        
        // 积分 = 金额 × 积分倍率
        return (int)(amount * pointsRate.doubleValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points, String source, Long relatedId, String description) {
        if (points <= 0) {
            return false;
        }
        
        UserMembership membership = userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
        );
        
        if (membership == null) {
            membership = createDefaultMembership(userId);
        }
        
        membership.setPoints(membership.getPoints() + points);
        membership.setTotalPoints(membership.getTotalPoints() + points);
        userMembershipMapper.updateById(membership);
        
        // 记录积分流水
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setType(1); // 获取
        log.setSource(source);
        log.setRelatedId(relatedId);
        log.setDescription(description);
        pointsLogMapper.insert(log);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumePoints(Long userId, Integer points, String source, Long relatedId, String description) {
        if (points <= 0) {
            return false;
        }
        
        UserMembership membership = userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
        );
        
        if (membership == null || membership.getPoints() < points) {
            throw new BusinessException("积分不足");
        }
        
        membership.setPoints(membership.getPoints() - points);
        membership.setUsedPoints(membership.getUsedPoints() + points);
        userMembershipMapper.updateById(membership);
        
        // 记录积分流水
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(-points); // 负数表示消费
        log.setType(2); // 消费
        log.setSource(source);
        log.setRelatedId(relatedId);
        log.setDescription(description);
        pointsLogMapper.insert(log);
        
        return true;
    }

    private UserMembership createDefaultMembership(Long userId) {
        UserMembership membership = new UserMembership();
        membership.setUserId(userId);
        membership.setLevelId(1L); // 默认普通会员
        membership.setPoints(0);
        membership.setTotalPoints(0);
        membership.setUsedPoints(0);
        membership.setGrowthValue(0);
        membership.setStatus(1);
        userMembershipMapper.insert(membership);
        return membership;
    }

    private UserMembershipVO convertToVO(UserMembership membership) {
        UserMembershipVO vo = new UserMembershipVO();
        BeanUtils.copyProperties(membership, vo);
        
        // 加载会员等级信息
        MembershipLevel level = membershipLevelMapper.selectById(membership.getLevelId());
        if (level != null) {
            vo.setLevelName(level.getLevelName());
            vo.setLevelCode(level.getLevelCode());
            vo.setDiscountRate(level.getDiscountRate());
            vo.setPointsRate(level.getPointsRate());
        }
        
        return vo;
    }

    private void autoUpgradeMembership(UserMembership membership) {
        List<MembershipLevel> levels = membershipLevelMapper.selectList(
            new LambdaQueryWrapper<MembershipLevel>()
                .le(MembershipLevel::getMinPoints, membership.getGrowthValue())
                .orderByDesc(MembershipLevel::getMinPoints)
                .last("LIMIT 1")
        );
        
        if (!levels.isEmpty()) {
            MembershipLevel highestLevel = levels.get(0);
            if (!highestLevel.getId().equals(membership.getLevelId())) {
                membership.setLevelId(highestLevel.getId());
                userMembershipMapper.updateById(membership);
            }
        }
    }
}
