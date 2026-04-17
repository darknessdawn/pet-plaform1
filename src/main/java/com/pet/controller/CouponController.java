package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.CouponService;
import com.pet.vo.CouponVO;
import com.pet.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Tag(name = "优惠券管理", description = "优惠券领取、查询等接口")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "获取优惠券列表")
    @GetMapping("/template/list")
    public Result<List<CouponVO>> getCouponTemplates() {
        List<CouponVO> list = couponService.getCouponTemplates();
        return Result.success(list);
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/receive/{templateId}")
    public Result<Boolean> receiveCoupon(@PathVariable Long templateId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean result = couponService.receiveCoupon(userId, templateId);
        return Result.success(result);
    }

    @Operation(summary = "我的优惠券")
    @GetMapping("/my/list")
    public Result<List<UserCouponVO>> getMyCoupons(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<UserCouponVO> list = couponService.getMyCoupons(userId, status);
        return Result.success(list);
    }

    @Operation(summary = "获取可用优惠券")
    @GetMapping("/my/available")
    public Result<List<UserCouponVO>> getAvailableCoupons(
            @RequestParam BigDecimal orderAmount,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<UserCouponVO> list = couponService.getAvailableCoupons(userId, orderAmount);
        return Result.success(list);
    }
}
