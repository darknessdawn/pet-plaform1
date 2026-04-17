package com.pet.controller;

import com.pet.common.Result;
import com.pet.dto.ReviewDTO;
import com.pet.service.ReviewService;
import com.pet.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论管理", description = "商品评论相关接口")
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "添加评论")
    @PostMapping("/add")
    public Result<ReviewVO> addReview(@Valid @RequestBody ReviewDTO reviewDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ReviewVO reviewVO = reviewService.addReview(userId, reviewDTO);
        return Result.success(reviewVO);
    }

    @Operation(summary = "更新评论")
    @PutMapping("/{reviewId}")
    public Result<ReviewVO> updateReview(@PathVariable Long reviewId,
                                         @Valid @RequestBody ReviewDTO reviewDTO,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ReviewVO reviewVO = reviewService.updateReview(userId, reviewId, reviewDTO);
        return Result.success(reviewVO);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        reviewService.deleteReview(userId, reviewId);
        return Result.success();
    }

    @Operation(summary = "获取商品的所有评论")
    @GetMapping("/product/{productId}")
    public Result<List<ReviewVO>> getProductReviews(@PathVariable Long productId) {
        List<ReviewVO> reviews = reviewService.getReviewsByProductId(productId);
        return Result.success(reviews);
    }

    @Operation(summary = "获取当前用户的评论列表")
    @GetMapping("/user")
    public Result<List<ReviewVO>> getUserReviews(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ReviewVO> reviews = reviewService.getReviewsByUserId(userId);
        return Result.success(reviews);
    }
}