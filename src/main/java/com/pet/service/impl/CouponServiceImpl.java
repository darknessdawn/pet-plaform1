package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.common.BusinessException;
import com.pet.entity.CouponTemplate;
import com.pet.entity.UserCoupon;
import com.pet.mapper.CouponTemplateMapper;
import com.pet.mapper.UserCouponMapper;
import com.pet.service.CouponService;
import com.pet.vo.CouponVO;
import com.pet.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements com.pet.service.CouponService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<CouponVO> getCouponTemplates() {
        List<CouponTemplate> templates = couponTemplateMapper.selectList(
            new LambdaQueryWrapper<CouponTemplate>()
                .eq(CouponTemplate::getStatus, 1)
                .eq(CouponTemplate::getDeleted, 0)
                .ge(CouponTemplate::getTotalCount, 0)
        );
        
        return templates.stream().map(template -> {
            CouponVO vo = new CouponVO();
            BeanUtils.copyProperties(template, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCoupon(Long userId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null || template.getDeleted() == 1) {
            throw new BusinessException("优惠券不存在");
        }
        
        if (template.getStatus() != 1) {
            throw new BusinessException("优惠券已停发");
        }
        
        if (template.getIssuedCount() >= template.getTotalCount()) {
            throw new BusinessException("优惠券已领完");
        }
        
        // 检查每人限领
        long receivedCount = userCouponMapper.selectCount(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponTemplateId, templateId)
        );
        
        if (receivedCount >= template.getPerUserLimit()) {
            throw new BusinessException("您已达到领取上限");
        }
        
        // 创建用户优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponTemplateId(templateId);
        userCoupon.setStatus(0); // 未使用
        userCoupon.setGetTime(LocalDateTime.now());
        userCoupon.setValidStart(template.getValidStart());
        userCoupon.setValidEnd(template.getValidEnd());
        
        userCouponMapper.insert(userCoupon);
        
        // 更新已发放数量
        template.setIssuedCount(template.getIssuedCount() + 1);
        couponTemplateMapper.updateById(template);
        
        return true;
    }

    @Override
    public List<UserCouponVO> getMyCoupons(Long userId, Integer status) {
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getGetTime)
        );
        
        List<UserCouponVO> result = new ArrayList<>();
        for (UserCoupon uc : userCoupons) {
            UserCouponVO vo = convertToVO(uc);
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public List<UserCouponVO> getAvailableCoupons(Long userId, BigDecimal orderAmount) {
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0) // 未使用
                .ge(UserCoupon::getValidEnd, LocalDateTime.now()) // 有效期内（validEnd >= now）
        );
        
        return userCoupons.stream()
            .filter(uc -> {
                CouponTemplate template = couponTemplateMapper.selectById(uc.getCouponTemplateId());
                return template != null && 
                       (template.getMinPurchase() == null || orderAmount.compareTo(template.getMinPurchase()) >= 0);
            })
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useCoupon(Long userId, Long couponId, Long orderId) {
        UserCoupon coupon = userCouponMapper.selectById(couponId);
        if (coupon == null || !coupon.getUserId().equals(userId)) {
            throw new BusinessException("优惠券不存在");
        }
        
        if (coupon.getStatus() != 0) {
            throw new BusinessException("优惠券不可用");
        }
        
        coupon.setStatus(1); // 已使用
        coupon.setOrderId(orderId);
        coupon.setUseTime(LocalDateTime.now());
        userCouponMapper.updateById(coupon);
        
        return true;
    }

    @Override
    public UserCouponVO calculateBestCoupon(Long userId, BigDecimal orderAmount) {
        List<UserCouponVO> available = getAvailableCoupons(userId, orderAmount);
        
        if (available.isEmpty()) {
            return null;
        }
        
        // 找出最优惠的券
        return available.stream()
            .max((c1, c2) -> {
                BigDecimal discount1 = calculateDiscount(c1, orderAmount);
                BigDecimal discount2 = calculateDiscount(c2, orderAmount);
                return discount1.compareTo(discount2);
            })
            .orElse(null);
    }

    @Override
    public CouponVO getCouponTemplateDetail(Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException("优惠券不存在");
        }
        
        CouponVO vo = new CouponVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount) {
        UserCoupon coupon = userCouponMapper.selectById(userCouponId);
        if (coupon == null) {
            return BigDecimal.ZERO;
        }
        
        CouponTemplate template = couponTemplateMapper.selectById(coupon.getCouponTemplateId());
        if (template == null) {
            return BigDecimal.ZERO;
        }
        
        // 检查是否满足使用条件
        if (template.getMinPurchase() != null && orderAmount.compareTo(template.getMinPurchase()) < 0) {
            return BigDecimal.ZERO;
        }
        
        if (template.getType() == 1) { // 满减
            return template.getDiscountAmount();
        } else if (template.getType() == 2) { // 折扣
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(template.getDiscountRate()));
            if (template.getMaxDiscount() != null) {
                discount = discount.min(template.getMaxDiscount());
            }
            return discount;
        }
        return BigDecimal.ZERO;
    }

    private UserCouponVO convertToVO(UserCoupon uc) {
        UserCouponVO vo = new UserCouponVO();
        BeanUtils.copyProperties(uc, vo);
        
        CouponTemplate template = couponTemplateMapper.selectById(uc.getCouponTemplateId());
        if (template != null) {
            vo.setCouponName(template.getName());
            vo.setType(template.getType());
            vo.setDiscountAmount(template.getDiscountAmount());
            vo.setDiscountRate(template.getDiscountRate());
            vo.setMinPurchase(template.getMinPurchase());
            vo.setMaxDiscount(template.getMaxDiscount());
        }
        
        return vo;
    }

    private BigDecimal calculateDiscount(UserCouponVO coupon, BigDecimal orderAmount) {
        if (coupon.getType() == 1) { // 满减
            return coupon.getDiscountAmount();
        } else if (coupon.getType() == 2) { // 折扣
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate()));
            if (coupon.getMaxDiscount() != null) {
                discount = discount.min(coupon.getMaxDiscount());
            }
            return discount;
        }
        return BigDecimal.ZERO;
    }
}
