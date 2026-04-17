package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.PointsService;
import com.pet.vo.PointsProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "积分商城", description = "积分商品兑换等接口")
public class PointsController {

    private final PointsService pointsService;

    @Operation(summary = "获取积分商城商品列表")
    @GetMapping("/products")
    public Result<List<PointsProductVO>> getPointsProducts() {
        List<PointsProductVO> list = pointsService.getPointsProducts();
        return Result.success(list);
    }

    @Operation(summary = "兑换积分商品")
    @PostMapping("/exchange/{productId}")
    public Result<Boolean> exchangePointsProduct(
            @PathVariable Long productId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean result = pointsService.exchangePointsProduct(userId, productId);
        return Result.success(result);
    }

    @Operation(summary = "我的积分订单")
    @GetMapping("/my/orders")
    public Result<List<Object>> getMyPointsOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<Object> list = pointsService.getMyPointsOrders(userId);
        return Result.success(list);
    }
}
