package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.RecommendationService;
import com.pet.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "推荐系统", description = "协同过滤推荐算法相关接口")
@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @Operation(summary = "获取个性化推荐 (混合推荐)")
    @GetMapping("/personal")
    public Result<List<ProductVO>> getPersonalRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProductVO> recommendations = recommendationService.recommendForUser(userId, limit);
        return Result.success(recommendations);
    }
    
    @Operation(summary = "基于UserCF的推荐")
    @GetMapping("/user-cf")
    public Result<List<ProductVO>> getUserCFRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProductVO> recommendations = recommendationService.recommendByUserCF(userId, limit);
        return Result.success(recommendations);
    }
    
    @Operation(summary = "基于ItemCF的推荐")
    @GetMapping("/item-cf")
    public Result<List<ProductVO>> getItemCFRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProductVO> recommendations = recommendationService.recommendByItemCF(userId, limit);
        return Result.success(recommendations);
    }
    
    @Operation(summary = "基于SVD的推荐")
    @GetMapping("/svd")
    public Result<List<ProductVO>> getSVDRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProductVO> recommendations = recommendationService.recommendBySVD(userId, limit);
        return Result.success(recommendations);
    }
    
    @Operation(summary = "获取相似商品")
    @GetMapping("/similar/{productId}")
    public Result<List<ProductVO>> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductVO> similarProducts = recommendationService.getSimilarProducts(productId, limit);
        return Result.success(similarProducts);
    }
    
    @Operation(summary = "获取热门推荐")
    @GetMapping("/hot")
    public Result<List<ProductVO>> getHotRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductVO> hotProducts = recommendationService.getHotRecommendations(limit);
        return Result.success(hotProducts);
    }
    
    @Operation(summary = "获取新品推荐")
    @GetMapping("/new")
    public Result<List<ProductVO>> getNewRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductVO> newProducts = recommendationService.getNewRecommendations(limit);
        return Result.success(newProducts);
    }
    
    @Operation(summary = "记录用户行为")
    @PostMapping("/behavior")
    public Result<Void> recordBehavior(
            @RequestParam Long productId,
            @RequestParam Integer behaviorType,
            @RequestParam(required = false) Double score,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        recommendationService.recordUserBehavior(userId, productId, behaviorType, score);
        return Result.success();
    }
}
