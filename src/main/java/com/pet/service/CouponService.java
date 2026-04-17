package com.pet.service;

import com.pet.dto.CouponDTO;
import com.pet.vo.CouponVO;
import com.pet.vo.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponService {

    /**
     * 获取优惠券列表
     */
    List<CouponVO> getCouponTemplates();

    /**
     * 领取优惠券
     */
    boolean receiveCoupon(Long userId, Long templateId);

    /**
     * 获取我的优惠券
     */
    List<UserCouponVO> getMyCoupons(Long userId, Integer status);

    /**
     * 获取可用的优惠券
     */
    List<UserCouponVO> getAvailableCoupons(Long userId, BigDecimal orderAmount);

    /**
     * 使用优惠券
     */
    boolean useCoupon(Long userId, Long couponId, Long orderId);

    /**
     * 计算最优优惠券
     */
    UserCouponVO calculateBestCoupon(Long userId, BigDecimal orderAmount);

    /**
     * 获取优惠券详情
     */
    CouponVO getCouponTemplateDetail(Long templateId);

    /**
     * 计算优惠券折扣金额
     */
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount);
}
